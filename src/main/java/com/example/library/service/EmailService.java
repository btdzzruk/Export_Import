package com.example.library.service;

import com.example.library.model.entity.Borrow;

public interface EmailService {
    void sendOverdueEmail(Borrow borrow);
}
