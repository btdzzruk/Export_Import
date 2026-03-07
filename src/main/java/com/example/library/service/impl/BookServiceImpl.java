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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    private static final int DEFAULT_PAGE_SIZE = 5;

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
            List<Book> books = bookRepository.findAll();

            if (books == null || books.isEmpty()) {
                throw new RuntimeException("Không có dữ liệu để xuất file Excel!");
            }

            List<ExportDTO> exportData = buildExportDTOs(books);

            if (exportData == null || exportData.isEmpty()) {
                throw new RuntimeException("Không thể chuyển đổi dữ liệu để xuất!");
            }

            ByteArrayOutputStream outputStream =
                    ExcelExporter.exportToExcel(exportData, null);

            if (outputStream == null || outputStream.toByteArray().length == 0) {
                throw new RuntimeException("Xuất Excel thất bại - file rỗng!");
            }

            return new ByteArrayResource(outputStream.toByteArray());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Lỗi khi xuất Excel: " + e.getMessage(), e);
        }
    }

    @Override
    public void importBooksFromExcel(MultipartFile file) throws IOException {

        try {

            if (file == null || file.isEmpty()) {
                throw new RuntimeException("File Excel không được rỗng!");
            }

            List<ImportBookDTO> importData =
                    ExcelImporter.importFromExcel(
                            file.getInputStream(),
                            ImportBookDTO.class
                    );

            if (importData == null || importData.isEmpty()) {
                throw new RuntimeException("File Excel không có dữ liệu!");
            }

            List<Book> books = buildBooks(importData);

            bookRepository.saveAll(books);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("Lỗi khi import Excel: " + e.getMessage(), e);
        }
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
}
