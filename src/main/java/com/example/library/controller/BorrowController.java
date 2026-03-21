package com.example.library.controller;

import com.example.library.model.request.BorrowSlipExportRequest;
import com.example.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @PostMapping("/export-slip")
    public ResponseEntity<ByteArrayResource> exportBorrowSlip(@RequestBody BorrowSlipExportRequest request) {
        ByteArrayResource file = borrowService.exportBorrowSlip(request);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=phieu-muon-sach.docx")
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .body(file);
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/return/{id}")
    public ResponseEntity<String> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return ResponseEntity.ok("Trả sách thành công !");
    }
}