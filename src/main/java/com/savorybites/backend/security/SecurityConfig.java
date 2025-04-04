package com.savorybites.backend.security;

import com.savorybites.backend.service.LogoutService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
public class SecurityConfig {
    private final JWTRequestFilter jwtRequestFilter;
    private final LogoutService logoutService;

    public SecurityConfig(JWTRequestFilter jwtRequestFilter, LogoutService logoutService) {
        this.jwtRequestFilter = jwtRequestFilter;
        this.logoutService = logoutService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers
                                (
                                        "/auth/register",
                                        "/auth/verifyEmail",
                                        "/auth/login",
                                        "/auth/verifyPassword",
                                        "auth/forgotPassword",
                                        "auth/resetPassword"
                                ).permitAll()
                        .anyRequest().authenticated() // Allow all requests without authentication
                )
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .addLogoutHandler(logoutService)
                        .logoutSuccessHandler(logoutService)
                );

        return http.build();
    }
}