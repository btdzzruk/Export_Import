package com.example.library.model.dto;

import com.example.library.excel.ColCellType;
import com.example.library.excel.ExcelColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportBookDTO {

    @ExcelColumn(col = 0, title = "STT", type = ColCellType._INTEGER)
    private Long STT;

    @ExcelColumn(col = 1, title = "Mã sách", type = ColCellType._STRING)
    private String code;

    @ExcelColumn(col = 2, title = "Tiêu đề", type = ColCellType._STRING)
    private String title;

    @ExcelColumn(col = 3, title = "Tác giả", type = ColCellType._STRING)
    private String author;

    @ExcelColumn(col = 4, title = "Số lượng", type = ColCellType._INTEGER)
    private Integer quantity;

    @ExcelColumn(col = 5, title = "Giá", type = ColCellType._DOLLARS)
    private Double price;

    @ExcelColumn(col = 6, title = "Status", type = ColCellType._STRING)
    private String status;

    @ExcelColumn(col = 7, title = "Description", type = ColCellType._STRING)
    private String description;
}
