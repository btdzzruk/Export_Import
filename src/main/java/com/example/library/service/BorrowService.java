package com.example.library.service;

import com.example.library.model.entity.Borrow;
import com.example.library.model.request.BorrowSlipExportRequest;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;

public interface BorrowService {

    void returnBook(Long borrowId);

    ByteArrayResource exportBorrowSlip(BorrowSlipExportRequest request);

    List<Borrow> findOverdueBorrows();

    void markNotified(Borrow borrow);
}