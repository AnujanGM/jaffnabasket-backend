package com.jaffnabasket.backend.cart.dto;

import java.time.Instant;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID productId,
        UUID variantId,
        int quantity,
        Instant addedAt
) {
}
