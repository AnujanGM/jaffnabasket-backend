package com.jaffnabasket.backend.product.dto;

import com.jaffnabasket.backend.product.entity.MediaType;

import java.util.UUID;

public record ProductMediaResponse(
        UUID id,
        String url,
        String altText,
        int sortOrder,
        MediaType type
) {
}
