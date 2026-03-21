package com.example.library.validation;

import com.example.library.model.dto.ImportMemberDTO;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class MemberImportValidation {

    private final Validator validator;

    public void validateImportData(
            List<ImportMemberDTO> batch,
            Set<String> existingCccd,
            Set<String> existingEmails,
            Set<String> existingPhones,
            Set<String> cccdInFile,
            Set<String> emailsInFile,
            Set<String> phonesInFile
    ) {

        for (ImportMemberDTO dto : batch) {

            List<String> errors = new ArrayList<>();

            // validate annotation trên DTO
            Set<ConstraintViolation<ImportMemberDTO>> violations =
                    validator.validate(dto);

            for (ConstraintViolation<ImportMemberDTO> violation : violations) {
                errors.add(violation.getMessage());
            }

            // check duplicate trong DB
            if (dto.getCccd() != null && existingCccd.contains(dto.getCccd())) {
                errors.add("CCCD đã tồn tại trong DB");
            }

            if (dto.getEmail() != null && existingEmails.contains(dto.getEmail())) {
                errors.add("Email đã tồn tại trong DB");
            }

            if (dto.getPhone() != null && existingPhones.contains(dto.getPhone())) {
                errors.add("Phone đã tồn tại trong DB");
            }

            // check duplicate trong file
            if (dto.getCccd() !=null && !cccdInFile.add(dto.getCccd())) {
                errors.add("CCCD bị trùng trong file");
            }

            if (dto.getEmail() != null && !emailsInFile.add(dto.getEmail())) {
                errors.add("Email bị trùng trong file");
            }

            if (dto.getPhone() != null && !phonesInFile.add(dto.getPhone())) {
                errors.add("Phone bị trùng trong file");
            }

            if (!errors.isEmpty()) {

                dto.setStatus("FAIL");
                dto.setDescription(String.join(" | ", errors));

            } else {

                dto.setStatus("PENDING");
            }
        }
    }
}