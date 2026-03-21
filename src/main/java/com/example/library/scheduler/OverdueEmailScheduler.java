package com.example.library.scheduler;

import com.example.library.model.entity.Borrow;
import com.example.library.service.BorrowService;
import com.example.library.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverdueEmailScheduler {

    private final BorrowService borrowService;
    private final EmailService emailService;

    // Chạy mỗi ngày lúc 8h sáng
    @Scheduled(cron = "0 0 8 * * ?")
    public void sendOverdueEmails() {
        for (Borrow borrow : borrowService.findOverdueBorrows()) {
            emailService.sendOverdueEmail(borrow);
            borrowService.markNotified(borrow);
        }
    }
}