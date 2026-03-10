package com.example.library.model;

import com.example.library.model.dto.ImportBookDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImportJobResult {
    private int totalRows;
    private int successCount;
    private int failCount;
    private List<ImportBookDTO> failedRows; // chỉ lưu row lỗi để xuất báo cáo
}
