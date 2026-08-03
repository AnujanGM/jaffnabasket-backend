package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.Locale;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProductTranslationRequest(
        @NotNull Locale locale,
        @NotBlank String name,
        String shortDescription,
        String longDescription,
        String seoTitle,
        String seoDescription
) {
}
