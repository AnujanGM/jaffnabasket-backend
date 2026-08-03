package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.ProductStatus;
import com.jaffnabasket.backend.product.entity.ProductType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductCreateRequest(
        @NotBlank String sku,
        @NotBlank String slug,
        @NotNull ProductType type,
        @NotNull ProductStatus status,
        UUID categoryId,
        UUID brandId,
        boolean exportable,
        boolean returnable,
        BigDecimal weight,
        String dimensions,
        @NotEmpty @Valid List<ProductTranslationRequest> translations
) {
}
