package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.ProductStatus;
import com.jaffnabasket.backend.product.entity.ProductType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProductDetailResponse(
        UUID id,
        String sku,
        String slug,
        ProductType type,
        ProductStatus status,
        String brandName,
        String brandSlug,
        String categoryName,
        String categorySlug,
        boolean exportable,
        boolean returnable,
        BigDecimal weight,
        String dimensions,
        String locale,
        String name,
        String shortDescription,
        String longDescription,
        String seoTitle,
        String seoDescription,
        List<ProductMediaResponse> media,
        List<ProductVariantResponse> variants,
        List<ProductAttributeResponse> attributes,
        Instant createdAt,
        Instant updatedAt
) {
}
