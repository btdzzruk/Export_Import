package com.example.library.service;

import org.springframework.core.io.ByteArrayResource;

public interface BorrowService {

    ByteArrayResource borrowBook(Long memberId, String bookCode);

    void returnBook(Long borrowId);
}