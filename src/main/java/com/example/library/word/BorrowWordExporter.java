package com.example.library.word;

import com.example.library.model.entity.Borrow;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class BorrowWordExporter {
    public ByteArrayResource exportBorrowSlip(Borrow borrow) {

        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);

            XWPFRun run = title.createRun();
            run.setText("Thông Tin Mượn Sách");
            run.setBold(true);
            run.setFontSize(18);

            XWPFParagraph info = document.createParagraph();

            XWPFRun infoRun = info.createRun();
            infoRun.setText("Thành viên: " + borrow.getMember().getFullName());
            infoRun.addBreak();

            infoRun.setText("Sách: " + borrow.getBook().getTitle());
            infoRun.addBreak();

            infoRun.setText("Tác giả: " + borrow.getBook().getAuthor());
            infoRun.addBreak();

            infoRun.setText("Ngày mượn: " + borrow.getBorrowDate());
            infoRun.addBreak();

            infoRun.setText("Trạng thái: " + borrow.getStatus());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            return new ByteArrayResource(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Export Word failed");
        }
    }
}
