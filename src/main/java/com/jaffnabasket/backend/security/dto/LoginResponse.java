package com.jaffnabasket.backend.security.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds
) {
}
