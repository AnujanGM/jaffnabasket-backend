package com.jaffnabasket.backend.product.dto;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String slug,
        UUID parentId
) {
}
