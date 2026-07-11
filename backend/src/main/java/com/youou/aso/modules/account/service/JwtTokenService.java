package com.youou.aso.modules.account.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtTokenService {
    private final String issuer;
    private final SecretKey secretKey;
    private final long ttlSeconds;

    public JwtTokenService(String issuer, String secret, long ttlSeconds) {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.issuer = issuer;
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
    }

    public String issue(Long accountId, String accountType, String roleCode) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(accountId))
                .claims(Map.of(
                        "accountType", accountType,
                        "roleCode", roleCode == null ? "" : roleCode
                ))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public AuthenticatedAccount parse(String token) {
        var claims = Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthenticatedAccount(
                Long.valueOf(claims.getSubject()),
                claims.get("accountType", String.class),
                claims.get("roleCode", String.class)
        );
    }
}
