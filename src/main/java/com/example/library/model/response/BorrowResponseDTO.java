package com.example.library.model.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class BorrowResponseDTO {

    private Long id;
    private String bookTitle;
    private String memberName;

    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;

    private String status;
    private Boolean notified;
}