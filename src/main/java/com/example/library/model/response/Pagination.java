package com.example.library.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Pagination {
    private int currentPage; // số trang hiện tại
    private int pageSize; // số phần từ mỗi trang
    private int totalPages; // tổng số trang có thể có
    private long totalItems; // tổng số bản ghi trong DB
}
