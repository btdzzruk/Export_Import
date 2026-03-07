package com.example.library.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class APIResponse <T>{
    private boolean success; // API chạy thành công hay thất bại
    private String message; // thông báo cho client
    private T data; // kiểu dữ liệu trả về (object, list, page)
    private final LocalDateTime timestamp = LocalDateTime.now(); // thời điêm trả về

    // Default constructor
    public APIResponse() {
    }

    // Constructor with parameters
    public APIResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
}
