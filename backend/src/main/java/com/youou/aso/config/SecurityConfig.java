package com.youou.aso.config;

import com.youou.aso.modules.account.service.JwtAuthenticationFilter;
import com.youou.aso.modules.account.service.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;
import java.util.Base64;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtTokenService jwtTokenService(
            @Value("${youou.security.jwt-issuer}") String issuer,
            @Value("${youou.security.jwt-secret:}") String secret,
            @Value("${youou.security.jwt-secret-file:${user.home}/.youou-aso/jwt-secret}") String secretFile,
            @Value("${youou.security.jwt-ttl-seconds:86400}") long ttlSeconds
    ) {
        return new JwtTokenService(issuer, resolveJwtSecret(secret, Path.of(secretFile)), ttlSeconds);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtTokenService jwtTokenService) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login",
                                "/api/auth/password-reset/code",
                                "/api/auth/password-reset/confirm"
                        ).permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/api/regions/enabled").permitAll()
                        .requestMatchers("/uploads/app-icons/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenService), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    static String resolveJwtSecret(String configuredSecret, Path secretFile) {
        if (configuredSecret != null && !configuredSecret.isBlank()) {
            return configuredSecret;
        }
        try {
            Path parent = secretFile.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            if (Files.exists(secretFile)) {
                return Files.readString(secretFile, StandardCharsets.UTF_8).trim();
            }
            String generatedSecret = generateSecret();
            try {
                Files.writeString(
                        secretFile,
                        generatedSecret,
                        StandardCharsets.UTF_8,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                );
                return generatedSecret;
            } catch (java.nio.file.FileAlreadyExistsException ignored) {
                return Files.readString(secretFile, StandardCharsets.UTF_8).trim();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read or create JWT secret file", exception);
        }
    }

    private static String generateSecret() {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
