package com.example.library.service.impl;

import com.example.library.mapper.BookMapper;
import com.example.library.model.entity.Book;
import com.example.library.model.request.BookAddDTO;
import com.example.library.model.request.BookUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import com.example.library.model.response.Pagination;
import com.example.library.repository.BookRepository;
import com.example.library.service.BookService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

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

    @Transactional
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

    @Transactional // đảm bảo tính toàn vẹn dữ liệu khi cập nhật
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

    @Transactional
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
}