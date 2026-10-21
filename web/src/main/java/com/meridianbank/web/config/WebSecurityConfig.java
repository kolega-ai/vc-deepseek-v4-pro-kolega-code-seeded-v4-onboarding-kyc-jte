package com.meridianbank.web.config;

import com.meridianbank.web.auth.SessionAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Channel-tier security: session-cookie authentication with CSRF enabled. The web tier
 * authenticates the user; the decision tier authorizes every action independently.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SessionAuthFilter sessionAuthFilter)
            throws Exception {
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/notifications/bulk-read"))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/register/**", "/reset-password",
                                "/error", "/css/**", "/js/**", "/fonts/**", "/images/**", "/favicon.ico",
                                "/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers("/admin/**").hasRole("BANK_ADMIN")
                        .requestMatchers("/officer/**").hasRole("ONBOARDING_OFFICER")
                        .requestMatchers("/compliance/**").hasRole("COMPLIANCE_OFFICER")
                        .requestMatchers("/customer/**").hasAnyRole("CUSTOMER", "PROSPECT")
                        .requestMatchers("/journey/**").hasAnyRole("PROSPECT", "CUSTOMER")
                        .requestMatchers("/profile/**", "/notifications/**", "/inbox/**").authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.logoutUrl("/perform-logout").disable())
                .addFilterBefore(sessionAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex.authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/login");
                }));
        return http.build();
    }
}
