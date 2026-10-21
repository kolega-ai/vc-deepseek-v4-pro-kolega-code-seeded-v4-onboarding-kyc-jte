package com.meridianbank.onboarding.config;

import com.meridianbank.onboarding.client.CoreMockClient;
import com.meridianbank.onboarding.client.ProviderMockClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class BeansConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public ProviderMockClient providerMockClient(AppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        RestClient restClient = RestClient.builder()
                .baseUrl(props.getProviders().getBaseUrl())
                .requestFactory(factory)
                .build();
        return new ProviderMockClient(restClient, props.getProviders().getApiKey());
    }

    @Bean
    public CoreMockClient coreMockClient(AppProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(15));
        RestClient restClient = RestClient.builder()
                .baseUrl(props.getCoreMock().getBaseUrl())
                .requestFactory(factory)
                .build();
        return new CoreMockClient(restClient, props.getCoreMock().getApiKey());
    }
}
