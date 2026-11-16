package com.example.paypulse.service.impl;

import com.example.paypulse.dto.email.RenderedEmail;
import com.example.paypulse.service.EmailTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ThymeleafEmailTemplateRenderer implements EmailTemplateRenderer {

    private static final Pattern TEMPLATE_ID = Pattern.compile("[a-z0-9]+(-[a-z0-9]+)*");
    private static final Locale LOCALE = Locale.ENGLISH;

    private final ITemplateEngine templateEngine;

    public ThymeleafEmailTemplateRenderer(@Qualifier("emailTemplateEngine") ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public RenderedEmail render(String templateId, Map<String, Object> variables) {
        if (templateId == null || !TEMPLATE_ID.matcher(templateId).matches()) {
            throw new IllegalArgumentException("Invalid email template id: " + templateId);
        }
        Context context = new Context(LOCALE, variables);
        String subject = templateEngine.process(templateId + "/subject.txt", context).strip();
        String html = inLayout("layout.html", templateEngine.process(templateId + "/body.html", context), subject);
        String text = inLayout("layout.txt", templateEngine.process(templateId + "/body.txt", context), subject);
        return new RenderedEmail(subject, html, text);
    }

    private String inLayout(String layout, String content, String subject) {
        Context context = new Context(LOCALE, Map.of("subject", subject, "content", content.strip()));
        return templateEngine.process(layout, context);
    }
}
