package com.jaffnabasket.backend.user.dto;

import com.jaffnabasket.backend.user.entity.UserStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String phone,
        UserStatus status,
        ProfileResponse profile,
        List<String> roles,
        Instant createdAt
) {
}
