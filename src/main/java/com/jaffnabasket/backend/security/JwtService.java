package com.jaffnabasket.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtService {

    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTokenExpiryMinutes;
    private final long refreshTokenExpiryDays;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                       @Value("${app.jwt.access-token-expiry-minutes}") long accessTokenExpiryMinutes,
                       @Value("${app.jwt.refresh-token-expiry-days}") long refreshTokenExpiryDays) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.accessTokenExpiryMinutes = accessTokenExpiryMinutes;
        this.refreshTokenExpiryDays = refreshTokenExpiryDays;
    }

    public String generateAccessToken(UUID userId, List<String> roles) {
        return buildToken(userId, roles, TOKEN_TYPE_ACCESS, getAccessTokenTtl());
    }

    public String generateRefreshToken(UUID userId, List<String> roles) {
        return buildToken(userId, roles, TOKEN_TYPE_REFRESH, getRefreshTokenTtl());
    }

    private String buildToken(UUID userId, List<String> roles, String tokenType, Duration ttl) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public boolean isAccessToken(Claims claims) {
        return TOKEN_TYPE_ACCESS.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return TOKEN_TYPE_REFRESH.equals(claims.get(CLAIM_TOKEN_TYPE, String.class));
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        return (List<String>) claims.get(CLAIM_ROLES, List.class);
    }

    public Duration getAccessTokenTtl() {
        return Duration.ofMinutes(accessTokenExpiryMinutes);
    }

    public Duration getRefreshTokenTtl() {
        return Duration.ofDays(refreshTokenExpiryDays);
    }
}
