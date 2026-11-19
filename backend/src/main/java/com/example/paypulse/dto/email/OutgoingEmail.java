package com.example.paypulse.dto.email;

public record OutgoingEmail(String to, String templateId, String subject, String html, String text) {
}
