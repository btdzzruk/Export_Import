package com.example.library.model.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookUpdateDTO {

    @NotBlank(message = "Mã sách không được để trống !")
    private String code;

    @NotBlank(message = "Tên tiêu đề không được để trống !")
    private String title;

    @NotBlank(message = "Tên tác giả không được để trống !")
    private String author;

    @NotNull(message = "Số lượng không được để trống !")
    @Min(value = 1, message = "Sức lượng phải > 0 !")
    private Integer quantity;

    @NotNull(message = "Giá không được để trống !")
    @Min(value = 100, message = "Giá phải > 100 !")
    private Double price;
}
