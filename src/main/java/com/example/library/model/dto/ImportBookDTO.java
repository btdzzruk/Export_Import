package com.example.library.model.dto;

import com.example.library.model.ColCellType;
import com.example.library.model.ExcelColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportBookDTO {

    @ExcelColumn(col = 0, title = "STT", type = ColCellType._INTEGER)
    private Long STT;

    @ExcelColumn(col = 1, title = "Mã sách", type = com.example.library.model.ColCellType._STRING)
    private String code;

    @ExcelColumn(col = 2, title = "Tiêu đề", type = com.example.library.model.ColCellType._STRING)
    private String title;

    @ExcelColumn(col = 3, title = "Tác giả", type = com.example.library.model.ColCellType._STRING)
    private String author;

    @ExcelColumn(col = 4, title = "Số lượng", type = com.example.library.model.ColCellType._INTEGER)
    private Integer quantity;

    @ExcelColumn(col = 5, title = "Giá", type = com.example.library.model.ColCellType._DOLLARS)
    private Double price;
}
