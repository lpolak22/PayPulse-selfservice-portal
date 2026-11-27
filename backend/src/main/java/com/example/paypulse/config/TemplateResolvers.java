package com.example.paypulse.config;

import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.nio.charset.StandardCharsets;
import java.util.Set;

final class TemplateResolvers {

    private TemplateResolvers() {
    }

    static ClassLoaderTemplateResolver classpath(String prefix, TemplateMode mode, String pattern, int order) {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix(prefix);
        resolver.setTemplateMode(mode);
        resolver.setResolvablePatterns(Set.of(pattern));
        resolver.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resolver.setCheckExistence(true);
        resolver.setCacheable(true);
        resolver.setOrder(order);
        return resolver;
    }
}
