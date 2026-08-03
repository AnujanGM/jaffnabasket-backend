package com.jaffnabasket.backend.user.dto;

import com.jaffnabasket.backend.user.entity.PreferredLanguage;

public record ProfileResponse(
        String firstName,
        String lastName,
        PreferredLanguage preferredLanguage,
        String avatarUrl
) {
}
