package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.ProductStatus;
import com.jaffnabasket.backend.product.entity.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AdminProductResponse(
        UUID id,
        String sku,
        String slug,
        ProductType type,
        ProductStatus status,
        UUID categoryId,
        UUID brandId,
        boolean exportable,
        boolean returnable,
        BigDecimal weight,
        String dimensions,
        Instant createdAt,
        Instant updatedAt
) {
}
