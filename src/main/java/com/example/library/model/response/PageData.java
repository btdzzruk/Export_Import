package com.example.library.model.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PageData <T>{
    private List<T> items; // danh sách dữ liệu
    private Pagination pagination;
}
