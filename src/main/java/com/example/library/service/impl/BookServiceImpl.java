package com.example.library.service.impl;

import com.example.library.mapper.BookMapper;
import com.example.library.model.ExcelExporter;
import com.example.library.model.ExcelImporter;
import com.example.library.model.dto.ExportDTO;
import com.example.library.model.dto.ImportBookDTO;
import com.example.library.model.entity.Book;
import com.example.library.model.request.BookAddDTO;
import com.example.library.model.request.BookUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.model.response.Pagination;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private static final int IMPORT_BATCH_SIZE = 500; // Mỗi chunk 500 rows

    @Override
    public APIResponse<PageData<Book>> findAllBook(int page, int size) {
        // PageRequest.of(pageIndex, size) -> pageIndex bắt đầu từ 0
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Book> bookPage = bookRepository.findAll(pageable);

        Pagination pagination = new Pagination();
        pagination.setCurrentPage(page);
        pagination.setPageSize(size);
        pagination.setTotalPages(bookPage.getTotalPages());
        pagination.setTotalItems(bookPage.getTotalElements());

        PageData<Book> pageData = new PageData<>();
        pageData.setItems(bookPage.getContent());
        pageData.setPagination(pagination);

        APIResponse<PageData<Book>> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Lấy danh sách thành công !");
        response.setData(pageData);
        return response;
    }

    @Override
    public APIResponse<Book> findBookById(Long id) {
        Book book = bookRepository.findById(id).orElse(null);

        APIResponse<Book> response = new APIResponse<>();
        if (book == null) {
            response.setSuccess(false);
            response.setMessage("Không tìm thấy sách với ID: " + id);
        } else {
            response.setSuccess(true);
            response.setMessage("Lấy thông tin sách thành công !");
            response.setData(book);
        }
        return response;
    }

    @Override
    public APIResponse<Book> addBook(BookAddDTO request) {
        // check trùng tiêu đề
        if (bookRepository.existsByTitle(request.getTitle())) {
            throw new RuntimeException("Tiêu đề sách đã tồn tại !");
        }
        Book book = bookMapper.mapToEntity(request);

        Book save = bookRepository.save(book);
        APIResponse<Book> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Thêm mới dữ liệu thành công !");
        response.setData(save);
        return response;
    }

    @Override
    public APIResponse<Book> updateBook(Long id, BookUpdateDTO request) {
        // check trùng tiêu đề
        if (bookRepository.existsByTitle(request.getTitle())) {
            throw new RuntimeException("Tiêu đề sách đã tồn tại !");
        }
        Book book = bookRepository.findById(id).orElseThrow(()->
                new RuntimeException("Không tìm thấy sách với ID: " + id));
        book.setCode(request.getCode());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setQuantity(request.getQuantity());
        book.setPrice(request.getPrice());
        bookRepository.save(book);

        APIResponse<Book> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Cập nhật dữ liệu thành công !");
        response.setData(book);
        return response;
    }

    @Override
    public APIResponse<String> deleteBookById(Long id) {
        Book book = bookRepository.findById(id).orElseThrow(()->
                new RuntimeException("Không tìm thấy sách với ID: " + id));
        bookRepository.delete(book);

        APIResponse<String> response = new APIResponse<>();
        response.setSuccess(true);
        response.setMessage("Xóa thành công !");
        response.setData("Đã xóa sinh viên với ID:" + id);
        return response;
    }

    @Override
    public ByteArrayResource exportBooksToExcel() throws IOException {

        try {

            int pageSize = 10_000;
            int pageNum = 0;

            List<List<ExportDTO>> pages = new ArrayList<>();

            AtomicLong index = new AtomicLong(1);

            Page<Book> page;

            do {

                page = bookRepository.findAll(PageRequest.of(pageNum++, pageSize));

                List<ExportDTO> dtoPage = page.getContent().stream().map(book -> {

                    ExportDTO dto = new ExportDTO();

                    dto.setSTT(index.getAndIncrement());
                    dto.setCode(book.getCode());
                    dto.setTitle(book.getTitle());
                    dto.setAuthor(book.getAuthor());
                    dto.setQuantity(book.getQuantity());
                    dto.setPrice(book.getPrice());

                    return dto;

                }).toList();

                pages.add(dtoPage);

            } while (page.hasNext());

            ByteArrayOutputStream outputStream =
                    ExcelExporter.exportStreaming(
                            pages,
                            ExportDTO.class,
                            "Books",
                            100
                    );
            return new ByteArrayResource(outputStream.toByteArray());

        } catch (Exception e) {
            throw new IOException("Lỗi khi export Excel: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER)
    public ByteArrayResource importBooksFromExcel(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không được trống !");
        }

        List<ImportBookDTO> result = new ArrayList<>();

        // cache dữ liệu trong DB để validate
        Set<String> existingTitles = bookRepository.findAllTitles();
        Set<String> titlesInFile = new HashSet<>();

        ExcelImporter.importStreaming(
                file.getInputStream(),
                ImportBookDTO.class,
                IMPORT_BATCH_SIZE,
                batch -> {

                    validateImportData(batch, existingTitles, titlesInFile);

                    int errorIndex = -1;

                    for (int i = 0; i < batch.size(); i++) {
                        if ("FAIL".equals(batch.get(i).getStatus())) {
                            errorIndex = i;
                            break;
                        }
                    }

                    if (errorIndex == -1) {

                        try {

                            saveBatchInTransaction(batch);

                            batch.forEach(dto -> {
                                dto.setStatus("SUCCESS");
                                dto.setDescription("Imported thành công !");
                            });

                        } catch (Exception e) {

                            batch.forEach(dto -> {
                                dto.setStatus("FAIL");
                                dto.setDescription("DB error: " + e.getMessage());
                            });
                        }

                    } else {

                        List<ImportBookDTO> successRows = new ArrayList<>();

                        for (int i = 0; i < batch.size(); i++) {

                            ImportBookDTO dto = batch.get(i);

                            if (i < errorIndex) {

                                dto.setStatus("SUCCESS");
                                dto.setDescription("Imported thành công !");
                                successRows.add(dto);

                            } else if (i > errorIndex) {

                                dto.setStatus("FAIL");
                                dto.setDescription("Batch này có lỗi ở row trước đó, nên không được import !");
                            }
                        }

                        if (!successRows.isEmpty()) {
                            saveBatchInTransaction(successRows);
                        }
                    }

                    result.addAll(batch);
                }
        );

        // export lại file kết quả
        ByteArrayOutputStream outputStream =
                ExcelExporter.exportToExcel(result, null);

        return new ByteArrayResource(outputStream.toByteArray());
    }

    private void validateImportData(
            List<ImportBookDTO> importData,
            Set<String> existingTitles,
            Set<String> titlesInFile) {

        for (ImportBookDTO dto : importData) {

            StringBuilder error = new StringBuilder();

            if (dto.getCode() == null || dto.getCode().isBlank())
                error.append("Mã sách đã tồn tại ! ");
            if (dto.getTitle() == null || dto.getTitle().isBlank()) {

                error.append("Tiêu đề sách đã tồn tại ! ");

            } else {

                if (existingTitles.contains(dto.getTitle()))
                    error.append("Tiêu đề đã tồn tại trong DB ! ");

                if (!titlesInFile.add(dto.getTitle()))
                    error.append("Tiêu đề bị trùng trong file ! ");
            }

            if (dto.getQuantity() == null || dto.getQuantity() <= 0)
                error.append("Số lượng phải > 0 ! ");

            if (dto.getPrice() == null || dto.getPrice() < 100)
                error.append("Giá phải >= 100 ! ");

            if (error.length() > 0) {

                dto.setStatus("FAIL");
                dto.setDescription(error.toString());

            } else {

                dto.setStatus("PENDING");
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBatchInTransaction(List<ImportBookDTO> batch) {

        List<Book> books = batch.stream().map(dto -> {

            Book book = new Book();

            book.setCode(dto.getCode());
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setQuantity(dto.getQuantity());
            book.setPrice(dto.getPrice());

            return book;

        }).collect(Collectors.toList());

        bookRepository.saveAll(books);
    }
}