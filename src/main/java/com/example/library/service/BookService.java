package com.example.library.service;

import com.example.library.model.entity.Book;
import com.example.library.model.request.BookAddDTO;
import com.example.library.model.request.BookUpdateDTO;
import com.example.library.model.response.APIResponse;
import com.example.library.model.response.PageData;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BookService {
    APIResponse<PageData<Book>> findAllBook(int page, int size);

    APIResponse<Book> findBookById(Long id);

    APIResponse<Book> addBook(BookAddDTO request);

    APIResponse<Book> updateBook(Long id, BookUpdateDTO request);

    APIResponse<String> deleteBookById(Long id);
}
