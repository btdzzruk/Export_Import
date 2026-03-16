package com.example.library.controller;

import com.example.library.service.BorrowService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping("/borrow")
    public ResponseEntity<ByteArrayResource> borrowBook(
            @RequestParam Long memberId,
            @RequestParam String bookCode
    ) {

        ByteArrayResource file = borrowService.borrowBook(memberId, bookCode);

        return ResponseEntity.ok()
                // Đặt header Content-Disposition để trình duyệt hiểu đây là file đính kèm
                .header("Content-Disposition", "attachment; filename=borrow-slip.docx")
                // Đảm bảo header Content-Type chính xác cho file Word
                .header("Content-Type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .body(file);
    }

    @PostMapping("/return/{id}")
    public ResponseEntity<String> returnBook(@PathVariable Long id) {
        borrowService.returnBook(id);
        return ResponseEntity.ok("Trả sách thành công !");
    }
}