package com.example.library.model.dto;

import com.example.library.excel.ColCellType;
import com.example.library.excel.ExcelColumn;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportMemberDTO {

    @ExcelColumn(col = 0, title = "STT", type = ColCellType._INTEGER)
    private Long STT;

    @NotBlank(message = "Mã thành viên không được để trống !")
    @ExcelColumn(col = 1, title = "Mã thành viên", type = ColCellType._STRING)
    private String code;

    @NotBlank(message = "Tên thành viên không được để trống !")
    @Size(min = 7, message = "Tên thành viên phải >= 7 ký tự !")
    @ExcelColumn(col = 2, title = "Tên thành viên", type = ColCellType._STRING)
    private String name;

    @NotBlank(message = "Email không được để trống !")
    @Email(message = "Email không đúng định dạng !")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$",
            message = "Email phải có định dạng @gmail.com !")
    @ExcelColumn(col = 3, title = "Email", type = ColCellType._STRING)
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống !")
    @Pattern(regexp = "^(03|08|09)[0-9]{8}$",
            message = "Số điện thoại phải bắt đầu 03, 08 hoặc 09 và có 10 chữ số !")
    @ExcelColumn(col = 4, title = "Số điện thoại", type = ColCellType._STRING)
    private String phone;

    @ExcelColumn(col = 5, title = "Status", type = ColCellType._STRING)
    private String status;

    @ExcelColumn(col = 6, title = "Description", type = ColCellType._STRING)
    private String description;
}