package com.jaffnabasket.backend.user.dto;

import com.jaffnabasket.backend.user.entity.PreferredLanguage;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 100) String firstName,
        @Size(max = 100) String lastName,
        PreferredLanguage preferredLanguage,
        String avatarUrl
) {
}
