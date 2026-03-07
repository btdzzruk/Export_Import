package com.example.library.mapper;

import com.example.library.model.entity.Book;
import com.example.library.model.request.BookAddDTO;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {
    public Book mapToEntity(BookAddDTO bookAddDTO) {
        Book book = new Book();
        book.setCode(bookAddDTO.getCode());
        book.setTitle(bookAddDTO.getTitle());
        book.setAuthor(bookAddDTO.getAuthor());
        book.setQuantity(bookAddDTO.getQuantity());
        book.setPrice(bookAddDTO.getPrice());
        return book;
    }
}
