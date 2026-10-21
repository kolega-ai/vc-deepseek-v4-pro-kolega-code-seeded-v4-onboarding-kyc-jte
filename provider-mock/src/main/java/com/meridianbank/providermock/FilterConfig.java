package com.meridianbank.providermock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@Configuration
public class FilterConfig {

    @Bean
    public FilterRegistrationBean<ProviderKeyFilter> providerKeyFilterRegistration(
            ProviderKeyFilter filter) {
        FilterRegistrationBean<ProviderKeyFilter> registration = new FilterRegistrationBean<>(filter);
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        return registration;
    }
}
