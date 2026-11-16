package com.example.paypulse.service;

import com.example.paypulse.dto.email.RenderedEmail;

import java.util.Map;

public interface EmailTemplateRenderer {

    RenderedEmail render(String templateId, Map<String, Object> variables);
}
