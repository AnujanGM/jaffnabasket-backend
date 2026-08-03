package com.jaffnabasket.backend.product.pagination;

import com.jaffnabasket.backend.exception.BadRequestException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/**
 * Opaque keyset cursor: base64("createdAtEpochMillis:id"). Callers should treat
 * the encoded string as opaque and never construct or parse it client-side.
 */
public final class CursorCodec {

    private CursorCodec() {
    }

    public record CursorPosition(Instant createdAt, UUID id) {
    }

    public static String encode(Instant createdAt, UUID id) {
        String raw = createdAt.toEpochMilli() + ":" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static CursorPosition decode(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split(":", 2);
            return new CursorPosition(Instant.ofEpochMilli(Long.parseLong(parts[0])), UUID.fromString(parts[1]));
        } catch (Exception e) {
            throw new BadRequestException("Invalid cursor");
        }
    }
}
