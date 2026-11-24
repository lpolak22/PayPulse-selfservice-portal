package com.example.paypulse.service.impl;

import com.example.paypulse.service.AppLinkPageService;
import com.example.paypulse.web.AppLinkTarget;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ThymeleafAppLinkPageService implements AppLinkPageService {

    private static final String APP_SCHEME = "paypulse";
    private static final String TEMPLATE = "open-app.html";
    private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_.~-]{1,4096}");

    private final ITemplateEngine templateEngine;

    public ThymeleafAppLinkPageService(@Qualifier("pageTemplateEngine") ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public boolean isWellFormedToken(String token) {
        return token != null && TOKEN.matcher(token).matches();
    }

    @Override
    public String render(AppLinkTarget target, String token) {
        Map<String, Object> variables = new HashMap<>();
        variables.put("title", target.title());
        variables.put("appLink", isWellFormedToken(token) ? appLink(target, token) : null);
        return templateEngine.process(TEMPLATE, new Context(Locale.ENGLISH, variables));
    }

    private static String appLink(AppLinkTarget target, String token) {
        return APP_SCHEME + "://" + target.path() + "?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }
}
