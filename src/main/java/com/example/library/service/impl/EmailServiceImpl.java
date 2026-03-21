package com.example.library.service.impl;

import com.example.library.model.entity.Borrow;
import com.example.library.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOverdueEmail(Borrow borrow) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(borrow.getMember().getEmail());
        message.setSubject("Thông báo trả sách quá hạn");

        message.setText(
                "Xin chào " + borrow.getMember().getFullName() + ",\n\n" +
                        "Bạn đã quá hạn trả sách:\n" +
                        "- Sách: " + borrow.getBook().getTitle() + "\n" +
                        "- Hạn trả: " + borrow.getDueDate() + "\n\n" +
                        "Vui lòng trả sớm nhất có thể.\n\nCảm ơn!"
        );

        mailSender.send(message);
    }
}