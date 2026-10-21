package com.meridianbank.web.config;

import gg.jte.ContentType;
import gg.jte.TemplateEngine;
import gg.jte.springframework.boot.autoconfigure.JteProperties;
import gg.jte.springframework.boot.autoconfigure.JteViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JTE configuration. The engine is created explicitly with {@link ContentType#Html} so every
 * template interpolation uses JTE's context-sensitive HTML output escaping
 * ({@code ${...}} escapes by content type; {@code $unsafe{...}} is never used for user content).
 * Templates are compiled to Java at build time by the gg.jte.gradle plugin.
 *
 * The view resolver is given an explicit order so it takes precedence over the servlet
 * container's fallback JSP resolver.
 */
@Configuration
public class JteConfig {

    @Bean
    public TemplateEngine jteTemplateEngine() {
        return TemplateEngine.createPrecompiled(ContentType.Html);
    }

    @Bean
    public JteViewResolver jteViewResolver(TemplateEngine templateEngine) {
        JteViewResolver resolver = new JteViewResolver(templateEngine, new JteProperties());
        resolver.setOrder(0);
        return resolver;
    }
}
