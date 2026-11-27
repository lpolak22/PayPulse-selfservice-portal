package com.example.paypulse.service;

import java.util.Map;

public interface EmailService {

    void send(String to, String templateId, Map<String, Object> variables);
}
