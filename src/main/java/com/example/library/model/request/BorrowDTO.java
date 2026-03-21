package com.example.library.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BorrowDTO {
    private Long bookId;
    private Long memberId;
    private LocalDateTime dueDate;
}
