package com.example.library.service;

import org.springframework.core.io.ByteArrayResource;

public interface BorrowRequestService {


    ByteArrayResource createRequest(String cccd, String bookCode);

    void approveRequest(Long requestId);

    void rejectRequest(Long requestId);
}
