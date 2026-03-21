package com.example.library.word;

import com.example.library.model.entity.Borrow;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Component
public class BorrowWordExporter {

    // Formatter cho LocalDateTime (có giờ)
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // Formatter cho LocalDate (chỉ ngày)
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ByteArrayResource exportBorrowSlip(Borrow borrow) {

        try (XWPFDocument document = new XWPFDocument()) {

            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText("PHIẾU MƯỢN SÁCH");
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            document.createParagraph();

            XWPFParagraph memberSection = document.createParagraph();
            XWPFRun memberRun = memberSection.createRun();
            memberRun.setText("THÔNG TIN NGƯỜI MƯỢN");
            memberRun.setBold(true);
            memberRun.setFontSize(12);

            document.createParagraph().createRun()
                    .setText("Họ và tên: " + borrow.getMember().getFullName());

            document.createParagraph().createRun()
                    .setText("CCCD: " + borrow.getMember().getCccd());

            document.createParagraph().createRun()
                    .setText("Email: " + borrow.getMember().getEmail());

            document.createParagraph().createRun()
                    .setText("Điện thoại: " + borrow.getMember().getPhone());

            document.createParagraph();

            XWPFParagraph bookSection = document.createParagraph();
            XWPFRun bookRun = bookSection.createRun();
            bookRun.setText("THÔNG TIN SÁCH");
            bookRun.setBold(true);
            bookRun.setFontSize(12);

            document.createParagraph().createRun()
                    .setText("Mã sách: " + borrow.getBook().getCode());

            document.createParagraph().createRun()
                    .setText("Tên sách: " + borrow.getBook().getTitle());

            document.createParagraph().createRun()
                    .setText("Tác giả: " + borrow.getBook().getAuthor());

            document.createParagraph().createRun()
                    .setText("Giá sách: " +
                            String.format("%,.0f", borrow.getBook().getPrice()) + " VNĐ");

            document.createParagraph();

            XWPFParagraph borrowSection = document.createParagraph();
            XWPFRun borrowRun = borrowSection.createRun();
            borrowRun.setText("THÔNG TIN MƯỢN");
            borrowRun.setBold(true);
            borrowRun.setFontSize(12);

            document.createParagraph().createRun()
                    .setText("Ngày mượn: " +
                            borrow.getBorrowDate().format(DATE_TIME_FORMATTER));

            document.createParagraph().createRun()
                    .setText("Hạn trả: " +
                            borrow.getDueDate().format(DATE_FORMATTER));

            document.createParagraph().createRun()
                    .setText("Trạng thái: " + borrow.getStatus());

            document.createParagraph();
            document.createParagraph();

            XWPFParagraph signature = document.createParagraph();
            signature.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun signatureRun = signature.createRun();
            signatureRun.setText("Người mượn xác nhận");
            signatureRun.setItalic(true);

            XWPFParagraph signatureName = document.createParagraph();
            signatureName.setAlignment(ParagraphAlignment.CENTER);
            signatureName.createRun().setText("(Ký tên)");

            // Xuất file Word ra ByteArrayResource
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.write(out);

            return new ByteArrayResource(out.toByteArray());

        } catch (Exception e) {
            throw new RuntimeException("Xuất file Word thất bại: " + e.getMessage(), e);
        }
    }
}