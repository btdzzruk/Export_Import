package com.example.library.word;

import com.example.library.model.entity.BorrowRequest;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class BorrowRequestWordExporter {

    public ByteArrayResource exportRequestSlip(BorrowRequest request) {

        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun run = title.createRun();
            run.setText("PHIẾU YÊU CẦU MƯỢN SÁCH");
            run.setBold(true);
            run.setFontSize(18);

            XWPFParagraph info = document.createParagraph();

            XWPFRun infoRun = info.createRun();
            infoRun.setText("Mã phiếu: " + request.getCode());
            infoRun.addBreak();

            infoRun.setText("CCCD: " + request.getCccd());
            infoRun.addBreak();

            infoRun.setText("Mã sách: " + request.getBookCode());
            infoRun.addBreak();

            infoRun.setText("Trạng thái: " + request.getStatus());
            infoRun.addBreak();

            infoRun.setText("Ngày tạo: " + request.getCreatedAt());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            return new ByteArrayResource(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Export Word failed");
        }
    }
}