package com.example.paypulse.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
@EnableAsync
@EnableConfigurationProperties(EmailProperties.class)
@PropertySource("classpath:email.properties")
public class EmailConfig {

    private static final String TEMPLATE_PREFIX = "templates/email/";

    @Bean
    public ITemplateEngine emailTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        engine.addTemplateResolver(TemplateResolvers.classpath(TEMPLATE_PREFIX, TemplateMode.HTML, "*.html", 1));
        engine.addTemplateResolver(TemplateResolvers.classpath(TEMPLATE_PREFIX, TemplateMode.TEXT, "*.txt", 2));
        return engine;
    }
}
