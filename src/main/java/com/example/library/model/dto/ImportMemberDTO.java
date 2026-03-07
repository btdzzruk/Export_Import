package com.example.library.model.dto;

import com.example.library.model.ColCellType;
import com.example.library.model.ExcelColumn;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportMemberDTO {

    @ExcelColumn(col = 0, title = "STT", type = ColCellType._INTEGER)
    private Long STT;

    @ExcelColumn(col = 1, title = "Mã thành viên", type = ColCellType._STRING)
    private String code;

    @ExcelColumn(col = 2, title = "Tên thành viên", type = ColCellType._STRING)
    private String name;

    @ExcelColumn(col = 3, title = "Email", type = ColCellType._STRING)
    private String email;

    @ExcelColumn(col = 4, title = "Số điện thoại", type = ColCellType._STRING)
    private String phone;
}
