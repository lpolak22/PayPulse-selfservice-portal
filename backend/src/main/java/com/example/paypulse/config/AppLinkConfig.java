package com.example.paypulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;

@Configuration
public class AppLinkConfig {

    private static final String TEMPLATE_PREFIX = "templates/page/";

    @Bean
    public ITemplateEngine pageTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();
        engine.addTemplateResolver(TemplateResolvers.classpath(TEMPLATE_PREFIX, TemplateMode.HTML, "*.html", 1));
        return engine;
    }
}
