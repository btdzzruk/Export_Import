package com.example.library.controller;

import com.example.library.service.BorrowRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow-requests")
@RequiredArgsConstructor
public class BorrowRequestController {

    private final BorrowRequestService borrowRequestService;

    // tạo phiếu tải file word
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/create")
    public ResponseEntity<ByteArrayResource> createRequest(
            @RequestParam String cccd,
            @RequestParam String bookCode) {

        ByteArrayResource file = borrowRequestService.createRequest(cccd, bookCode);

        return ResponseEntity.ok().body(file);
    }

    // duyệt phiếu yêu cầu
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/approve/{id}")
    public ResponseEntity<String> approveRequest(@PathVariable Long id) {
        borrowRequestService.approveRequest(id);
        return ResponseEntity.ok("Duyệt phiếu thành công !");
    }

    // từ chối phiếu yêu cầu
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reject/{id}")
    public ResponseEntity<String> rejectRequest(@PathVariable Long id) {

        borrowRequestService.rejectRequest(id);

        return ResponseEntity.ok("Đã từ chối phiếu!");
    }
}
