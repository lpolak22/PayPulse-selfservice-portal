package com.example.paypulse.service;

import com.example.paypulse.dto.email.EmailMessage;

public interface EmailSender {

    void send(EmailMessage message);
}
