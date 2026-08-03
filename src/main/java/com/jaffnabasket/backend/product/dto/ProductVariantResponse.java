package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.StockPolicy;

import java.util.Map;
import java.util.UUID;

public record ProductVariantResponse(
        UUID id,
        String sku,
        Map<String, String> attributes,
        StockPolicy stockPolicy
) {
}
