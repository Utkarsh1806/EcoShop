package com.ecoshop.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService {

    private final SecretKey key;
    private final long ttlSeconds;
    private final String issuer;

    public JwtService(
            @Value("${ecoshop.jwt.secret:change-me-change-me-change-me-change-me}") String secret,
            @Value("${ecoshop.jwt.ttl-seconds:3600}") long ttlSeconds,
            @Value("${ecoshop.jwt.issuer:ecoshop}") String issuer) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlSeconds = ttlSeconds;
        this.issuer = issuer;
    }

    public String issue(String subject, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(issuer)
                .subject(subject)
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
