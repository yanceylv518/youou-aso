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
        return issue(accountId, accountType, roleCode, null);
    }

    public String issue(Long accountId, String accountType, String roleCode, String passwordHash) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(accountId))
                .claims(Map.of(
                        "accountType", accountType,
                        "roleCode", roleCode == null ? "" : roleCode
                ))
                .claim("credential", passwordHash == null ? null : credentialProof(passwordHash))
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(secretKey)
                .compact();
    }

    private io.jsonwebtoken.Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean matchesCredential(String token, String passwordHash) {
        String proof = claims(token).get("credential", String.class);
        return proof != null && passwordHash != null && java.security.MessageDigest.isEqual(
                proof.getBytes(StandardCharsets.UTF_8), credentialProof(passwordHash).getBytes(StandardCharsets.UTF_8));
    }

    private String credentialProof(String passwordHash) {
        try {
            var mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(secretKey.getEncoded(), "HmacSHA256"));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(passwordHash.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public AuthenticatedAccount parse(String token) {
        var claims = claims(token);
        return new AuthenticatedAccount(
                Long.valueOf(claims.getSubject()),
                claims.get("accountType", String.class),
                claims.get("roleCode", String.class)
        );
    }
}
