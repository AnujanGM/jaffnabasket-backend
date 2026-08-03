package com.jaffnabasket.backend.cart.controller;

import com.jaffnabasket.backend.cart.dto.AddCartItemRequest;
import com.jaffnabasket.backend.cart.dto.CartResponse;
import com.jaffnabasket.backend.cart.dto.UpdateCartItemQuantityRequest;
import com.jaffnabasket.backend.cart.service.CartService;
import com.jaffnabasket.backend.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                 @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        return withGuestTokenHeader(cartService.getCart(userId(principal), guestToken));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                 @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
                                                 @Valid @RequestBody AddCartItemRequest request) {
        return withGuestTokenHeader(cartService.addOrUpdateItem(userId(principal), guestToken, request));
    }

    @PatchMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateItemQuantity(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                             @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
                                                             @PathVariable UUID id,
                                                             @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        return withGuestTokenHeader(cartService.updateItemQuantity(userId(principal), guestToken, id, request));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> removeItem(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                     @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
                                                     @PathVariable UUID id) {
        return withGuestTokenHeader(cartService.removeItem(userId(principal), guestToken, id));
    }

    private UUID userId(CustomUserPrincipal principal) {
        return principal != null ? principal.getId() : null;
    }

    private ResponseEntity<CartResponse> withGuestTokenHeader(CartResponse response) {
        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        if (response.guestToken() != null) {
            builder.header(GUEST_TOKEN_HEADER, response.guestToken());
        }
        return builder.body(response);
    }
}
