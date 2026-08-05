package com.jaffnabasket.backend.cart.controller;

import com.jaffnabasket.backend.cart.dto.AddCartItemRequest;
import com.jaffnabasket.backend.cart.dto.CartResponse;
import com.jaffnabasket.backend.cart.dto.UpdateCartItemQuantityRequest;
import com.jaffnabasket.backend.cart.service.CartService;
import com.jaffnabasket.backend.security.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Cart", description = "Shopping cart for both guests and logged-in users")
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private static final String GUEST_TOKEN_HEADER = "X-Guest-Token";

    private final CartService cartService;

    @Operation(
            summary = "Get the current cart",
            description = "If you're logged in (Authorization header present), returns your user's cart. Otherwise, "
                    + "returns the cart for the guest identified by the X-Guest-Token header. If neither is present, "
                    + "creates a brand new empty guest cart and returns its token in the X-Guest-Token response header "
                    + "- save it and send it back on future guest requests."
    )
    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Guest cart token from a previous response's X-Guest-Token header. Not needed if logged in.")
            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken) {
        return withGuestTokenHeader(cartService.getCart(userId(principal), guestToken));
    }

    @Operation(
            summary = "Add or update a cart line",
            description = "Adds a product (and optional variantId) to the cart. If that exact product/variant "
                    + "combination is already in the cart, this overwrites its quantity rather than adding to it."
    )
    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Guest cart token from a previous response's X-Guest-Token header. Not needed if logged in.")
            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
            @Valid @RequestBody AddCartItemRequest request) {
        return withGuestTokenHeader(cartService.addOrUpdateItem(userId(principal), guestToken, request));
    }

    @Operation(
            summary = "Change a cart line's quantity",
            description = "Updates the quantity of an existing cart item, identified by its own id (not the product id)."
    )
    @PatchMapping("/items/{id}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Guest cart token from a previous response's X-Guest-Token header. Not needed if logged in.")
            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
            @Parameter(description = "UUID of the cart item (from the cart's items list), not the product id") @PathVariable UUID id,
            @Valid @RequestBody UpdateCartItemQuantityRequest request) {
        return withGuestTokenHeader(cartService.updateItemQuantity(userId(principal), guestToken, id, request));
    }

    @Operation(
            summary = "Remove a cart line",
            description = "Deletes a single item from the cart, identified by its own id (not the product id)."
    )
    @DeleteMapping("/items/{id}")
    public ResponseEntity<CartResponse> removeItem(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Guest cart token from a previous response's X-Guest-Token header. Not needed if logged in.")
            @RequestHeader(value = GUEST_TOKEN_HEADER, required = false) String guestToken,
            @Parameter(description = "UUID of the cart item (from the cart's items list), not the product id") @PathVariable UUID id) {
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
