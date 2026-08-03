package com.jaffnabasket.backend.cart.dto;

import jakarta.validation.constraints.Min;

public record UpdateCartItemQuantityRequest(
        @Min(1) int quantity
) {
}
