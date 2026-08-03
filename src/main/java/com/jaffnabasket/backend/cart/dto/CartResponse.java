package com.jaffnabasket.backend.cart.dto;

import com.jaffnabasket.backend.cart.entity.CartStatus;

import java.util.List;
import java.util.UUID;

public record CartResponse(
        UUID id,
        UUID userId,
        String guestToken,
        CartStatus status,
        List<CartItemResponse> items
) {
}
