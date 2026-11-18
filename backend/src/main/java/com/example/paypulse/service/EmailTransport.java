package com.example.paypulse.service;

import com.example.paypulse.dto.email.OutgoingEmail;

public interface EmailTransport {

    void deliver(OutgoingEmail email);
}
