package com.jaffnabasket.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        String secret = Base64.getEncoder().encodeToString("test-secret-key-that-is-long-enough-1234567890".getBytes());
        jwtService = new JwtService(secret, 15, 7);
    }

    @Test
    void generatesAndParsesAccessToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateAccessToken(userId, List.of("CUSTOMER"));

        Claims claims = jwtService.parseClaims(token);

        assertThat(jwtService.getUserId(claims)).isEqualTo(userId);
        assertThat(jwtService.getRoles(claims)).containsExactly("CUSTOMER");
        assertThat(jwtService.isAccessToken(claims)).isTrue();
        assertThat(jwtService.isRefreshToken(claims)).isFalse();
    }

    @Test
    void generatesAndParsesRefreshToken() {
        UUID userId = UUID.randomUUID();
        String token = jwtService.generateRefreshToken(userId, List.of("CUSTOMER"));

        Claims claims = jwtService.parseClaims(token);

        assertThat(jwtService.isRefreshToken(claims)).isTrue();
        assertThat(jwtService.isAccessToken(claims)).isFalse();
    }

    @Test
    void rejectsTokenSignedWithADifferentSecret() {
        String otherSecret = Base64.getEncoder().encodeToString("a-completely-different-secret-key-value-here".getBytes());
        JwtService otherJwtService = new JwtService(otherSecret, 15, 7);
        String token = otherJwtService.generateAccessToken(UUID.randomUUID(), List.of("CUSTOMER"));

        assertThatThrownBy(() -> jwtService.parseClaims(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsMalformedToken() {
        assertThatThrownBy(() -> jwtService.parseClaims("not-a-valid-jwt")).isInstanceOf(JwtException.class);
    }
}
