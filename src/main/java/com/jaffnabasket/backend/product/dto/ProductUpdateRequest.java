package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.ProductStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductUpdateRequest(
        String slug,
        ProductStatus status,
        UUID categoryId,
        UUID brandId,
        Boolean exportable,
        Boolean returnable,
        BigDecimal weight,
        String dimensions
) {
}
