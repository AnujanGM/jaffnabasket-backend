package com.jaffnabasket.backend.product.dto;

import java.util.UUID;

public record BrandResponse(
        UUID id,
        String name,
        String slug
) {
}
