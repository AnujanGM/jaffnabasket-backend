package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.ProductType;

import java.util.UUID;

public record ProductSummaryResponse(
        UUID id,
        String sku,
        String slug,
        ProductType type,
        String brandName,
        String brandSlug,
        String name,
        String shortDescription,
        String primaryImageUrl
) {
}
