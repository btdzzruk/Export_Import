package com.example.library.validation;

import com.example.library.model.dto.ImportBookDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class BookImportValidator {

    public void validate(
            List<ImportBookDTO> importData,
            Set<String> existingTitles,
            Set<String> titlesInFile) {

        for (ImportBookDTO dto : importData) {

            StringBuilder error = new StringBuilder();

            if (dto.getCode() == null || dto.getCode().isBlank()) {
                error.append("Mã sách không được để trống ! ");
            }

            if (dto.getTitle() == null || dto.getTitle().isBlank()) {

                error.append("Tiêu đề sách không được để trống ! ");

            } else {

                if (existingTitles.contains(dto.getTitle()))
                    error.append("Tiêu đề đã tồn tại trong DB ! ");

                if (!titlesInFile.add(dto.getTitle()))
                    error.append("Tiêu đề bị trùng trong file ! ");
            }

            if (dto.getQuantity() == null || dto.getQuantity() <= 0)
                error.append("Số lượng phải > 0 ! ");

            if (dto.getPrice() == null || dto.getPrice() < 100)
                error.append("Giá phải >= 100 ! ");

            if (error.length() > 0) {

                dto.setStatus("FAIL");
                dto.setDescription(error.toString());

            } else {

                dto.setStatus("PENDING");
            }
        }
    }
}
