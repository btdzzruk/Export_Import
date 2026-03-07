package com.example.library.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberUpdateDTO {

    @NotBlank(message = "Mã thành viên không được để trống !")
    private String code;

    @NotBlank(message = "Họ tên không được để trống !")
    @Size(min = 7, message = "Họ tên phải có ít nhất 7 ký tự !")
    private String fullName;

    @NotBlank(message = "Email không được để trống !")
    @Email(message = "Email không đúng định dạng!")
    @Pattern(regexp = "^[A-Za-z0-9+_.-]+@gmail\\.com$",
            message = "Email phải có định dạng @gmail.com")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống !")
    @Pattern(regexp = "^(03|08|09)[0-9]{8}$",
            message = "Số điện thoại phải bắt đầu bằng 03, 08 hoặc 09 và có 10 chữ số !")
    private String phone;
}
