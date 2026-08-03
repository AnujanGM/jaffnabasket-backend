package com.jaffnabasket.backend.security.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
        @NotBlank String identifier
) {
}
