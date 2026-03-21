package com.example.library.model.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BorrowSlipExportRequest {
    private String cccd;              // CCCD của người mượn (unique identifier)
    private String bookCode;           // Mã sách
    private LocalDate dueDate;    // Hạn trả sách
}

