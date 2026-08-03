package com.jaffnabasket.backend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @Email String email,
        String phone,
        @NotBlank @Size(min = 8, max = 100) String password
) {
}
