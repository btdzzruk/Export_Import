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
            // Stream từng page thay vì load all vào RAM
            List<ExportDTO> exportData = new ArrayList<>();
            AtomicLong index = new AtomicLong(1);
            int pageSize = 10_000;
            int pageNum = 0;

            Page<Book> page;
            do {
                page = bookRepository.findAll(PageRequest.of(pageNum++, pageSize));
                page.getContent().forEach(book -> {
                    ExportDTO dto = new ExportDTO();
                    dto.setSTT(index.getAndIncrement());
                    dto.setCode(book.getCode());
                    dto.setTitle(book.getTitle());
                    dto.setAuthor(book.getAuthor());
                    dto.setQuantity(book.getQuantity());
                    dto.setPrice(book.getPrice());
                    exportData.add(dto);
                });
            } while (page.hasNext());

            if (exportData.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu để xuất file Excel!");
            }

            ByteArrayOutputStream outputStream =
                    ExcelExporter.exportToExcelMultiSheet(exportData, "Books");

            return new ByteArrayResource(outputStream.toByteArray());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Lỗi khi xuất Excel: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.NEVER) // Không wrap toàn bộ trong 1 transaction
    public ByteArrayResource importBooksFromExcel(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File Excel không được rỗng!");
        }

        List<ImportBookDTO> importData = ExcelImporter.importFromExcel(
                file.getInputStream(), ImportBookDTO.class
        );

        if (importData.isEmpty()) {
            throw new RuntimeException("File Excel không có dữ liệu!");
        }

        // Validate trước — không chạm DB
        validateImportData(importData);

        // Lưu theo chunk, mỗi chunk là 1 transaction độc lập
        processInChunks(importData);

        // Xuất báo cáo kết quả
        ByteArrayOutputStream outputStream = ExcelExporter.exportToExcel(importData, null);
        return new ByteArrayResource(outputStream.toByteArray());
    }

    // Xây dựng danh sách ExportDTO từ danh sách Book để xuất Excel
    private List<ExportDTO> buildExportDTOs(List<Book> books) {
        AtomicLong index = new AtomicLong(1);
        return books.stream().map(book -> {
            ExportDTO dto = new ExportDTO();
            dto.setSTT(index.getAndIncrement());
            dto.setCode(book.getCode());
            dto.setTitle(book.getTitle());
            dto.setAuthor(book.getAuthor());
            dto.setQuantity(book.getQuantity());
            dto.setPrice(book.getPrice());
            return dto;
        }).toList();
    }

    // Kiểm tra trùng tiêu đề và xây dựng danh sách Book từ dữ liệu import
    private List<Book> buildBooks(List<ImportBookDTO> importData) {

        return importData.stream().map(dto -> {

            if (bookRepository.existsByTitle(dto.getTitle())) {
                throw new RuntimeException("Sách đã tồn tại: " + dto.getTitle());
            }

            Book book = new Book();

            book.setCode(dto.getCode());
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setQuantity(dto.getQuantity());
            book.setPrice(dto.getPrice());

            return book;

        }).toList();
    }

    private void validateImportData(List<ImportBookDTO> importData) {

        // Cache title đã tồn tại trong DB (1 lần query duy nhất)
        Set<String> existingTitles = bookRepository.findAllTitles(); // query custom

        // Cache title trong chính file để bắt duplicate nội bộ
        Set<String> seenTitlesInFile = new HashSet<>();

        for (ImportBookDTO dto : importData) {
            StringBuilder error = new StringBuilder();

            if (dto.getCode() == null || dto.getCode().isBlank())
                error.append("Mã sách không được để trống; ");

            if (dto.getTitle() == null || dto.getTitle().isBlank()) {
                error.append("Tiêu đề sách không được để trống; ");
            } else {
                if (existingTitles.contains(dto.getTitle()))
                    error.append("Tiêu đề đã tồn tại trong DB; ");
                if (!seenTitlesInFile.add(dto.getTitle()))
                    error.append("Tiêu đề bị trùng trong file; ");
            }

            if (dto.getAuthor() == null || dto.getAuthor().isBlank())
                error.append("Tác giả không được để trống; ");

            if (dto.getQuantity() == null || dto.getQuantity() <= 0)
                error.append("Số lượng phải > 0; ");

            if (dto.getPrice() == null || dto.getPrice() < 100)
                error.append("Giá phải >= 100; ");

            if (error.length() > 0) {
                dto.setStatus("FAIL");
                dto.setDescription(error.toString());
            } else {
                dto.setStatus("PENDING"); // Chờ lưu
            }
        }
    }

    private void processInChunks(List<ImportBookDTO> importData) {

        List<ImportBookDTO> pendingRows = importData.stream()
                .filter(dto -> "PENDING".equals(dto.getStatus()))
                .collect(Collectors.toList());

        // Chia thành batches
        List<List<ImportBookDTO>> batches = ListUtils.partition(pendingRows, IMPORT_BATCH_SIZE);
        // Hoặc dùng Guava: Lists.partition(pendingRows, IMPORT_BATCH_SIZE)

        int chunkIndex = 0;
        for (List<ImportBookDTO> batch : batches) {
            try {
                saveBatchInTransaction(batch); // Mỗi chunk commit độc lập
                batch.forEach(dto -> {
                    dto.setStatus("SUCCESS");
                    dto.setDescription("Imported successfully");
                });
                log.info("Chunk {}/{} saved: {} rows", ++chunkIndex, batches.size(), batch.size());

            } catch (Exception e) {
                // Chunk này lỗi → đánh dấu tất cả row trong chunk là FAIL
                // Các chunk trước đó đã commit an toàn
                log.error("Chunk {} failed: {}", chunkIndex, e.getMessage());
                batch.forEach(dto -> {
                    dto.setStatus("FAIL");
                    dto.setDescription("Chunk error: " + e.getMessage());
                });
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // Transaction mới, độc lập
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

        bookRepository.saveAll(books); // 1 batch insert
    }
}
