package com.jaffnabasket.backend.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddCartItemRequest(
        @NotNull UUID productId,
        UUID variantId,
        @Min(1) int quantity
) {
}
