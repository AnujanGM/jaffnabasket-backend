package com.jaffnabasket.backend.security.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String identifier,
        @NotBlank String password
) {
}
