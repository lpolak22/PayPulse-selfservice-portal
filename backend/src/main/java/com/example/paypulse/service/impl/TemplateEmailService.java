package com.example.paypulse.service.impl;

import com.example.paypulse.dto.email.OutgoingEmail;
import com.example.paypulse.dto.email.RenderedEmail;
import com.example.paypulse.service.EmailService;
import com.example.paypulse.service.EmailTemplateRenderer;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TemplateEmailService implements EmailService {

    private final EmailTemplateRenderer renderer;
    private final ApplicationEventPublisher events;

    public TemplateEmailService(EmailTemplateRenderer renderer, ApplicationEventPublisher events) {
        this.renderer = renderer;
        this.events = events;
    }

    @Override
    public void send(String to, String templateId, Map<String, Object> variables) {
        RenderedEmail rendered = renderer.render(templateId, variables);
        events.publishEvent(new OutgoingEmail(to, templateId, rendered.subject(), rendered.html(), rendered.text()));
    }
}
