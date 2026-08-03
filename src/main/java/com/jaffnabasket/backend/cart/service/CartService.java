package com.jaffnabasket.backend.cart.service;

import com.jaffnabasket.backend.cart.dto.AddCartItemRequest;
import com.jaffnabasket.backend.cart.dto.CartItemResponse;
import com.jaffnabasket.backend.cart.dto.CartResponse;
import com.jaffnabasket.backend.cart.dto.UpdateCartItemQuantityRequest;
import com.jaffnabasket.backend.cart.entity.Cart;
import com.jaffnabasket.backend.cart.entity.CartItem;
import com.jaffnabasket.backend.cart.entity.CartStatus;
import com.jaffnabasket.backend.cart.repository.CartItemRepository;
import com.jaffnabasket.backend.cart.repository.CartRepository;
import com.jaffnabasket.backend.exception.ResourceNotFoundException;
import com.jaffnabasket.backend.product.entity.Product;
import com.jaffnabasket.backend.product.entity.ProductVariant;
import com.jaffnabasket.backend.product.repository.ProductRepository;
import com.jaffnabasket.backend.product.repository.ProductVariantRepository;
import com.jaffnabasket.backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    @Transactional
    public CartResponse getCart(UUID authenticatedUserId, String guestToken) {
        Cart cart = resolveOrCreateCart(authenticatedUserId, guestToken);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addOrUpdateItem(UUID authenticatedUserId, String guestToken, AddCartItemRequest request) {
        Cart cart = resolveOrCreateCart(authenticatedUserId, guestToken);
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        ProductVariant variant = null;
        if (request.variantId() != null) {
            variant = productVariantRepository.findById(request.variantId())
                    .orElseThrow(() -> new ResourceNotFoundException("Variant not found"));
        }

        Optional<CartItem> existing = request.variantId() == null
                ? cartItemRepository.findByCart_IdAndProduct_IdAndVariantIsNull(cart.getId(), product.getId())
                : cartItemRepository.findByCart_IdAndProduct_IdAndVariant_Id(cart.getId(), product.getId(), request.variantId());

        CartItem item = existing.orElseGet(() -> CartItem.builder()
                .cart(cart)
                .product(product)
                .build());
        item.setVariant(variant);
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID authenticatedUserId, String guestToken, UUID itemId,
                                            UpdateCartItemQuantityRequest request) {
        Cart cart = resolveOrCreateCart(authenticatedUserId, guestToken);
        CartItem item = findItemInCart(cart, itemId);
        item.setQuantity(request.quantity());
        cartItemRepository.save(item);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID authenticatedUserId, String guestToken, UUID itemId) {
        Cart cart = resolveOrCreateCart(authenticatedUserId, guestToken);
        CartItem item = findItemInCart(cart, itemId);
        cartItemRepository.delete(item);
        return toResponse(cart);
    }

    private CartItem findItemInCart(Cart cart, UUID itemId) {
        return cartItemRepository.findById(itemId)
                .filter(item -> item.getCart().getId().equals(cart.getId()))
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
    }

    private Cart resolveOrCreateCart(UUID authenticatedUserId, String guestToken) {
        if (authenticatedUserId != null) {
            return cartRepository.findByUser_Id(authenticatedUserId)
                    .orElseGet(() -> cartRepository.save(Cart.builder()
                            .user(userRepository.getReferenceById(authenticatedUserId))
                            .status(CartStatus.ACTIVE)
                            .build()));
        }
        if (guestToken != null && !guestToken.isBlank()) {
            Optional<Cart> existing = cartRepository.findByGuestToken(guestToken);
            if (existing.isPresent()) {
                return existing.get();
            }
        }
        return cartRepository.save(Cart.builder()
                .guestToken(UUID.randomUUID().toString())
                .status(CartStatus.ACTIVE)
                .build());
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cartItemRepository.findByCart_Id(cart.getId()).stream()
                .map(item -> new CartItemResponse(item.getId(), item.getProduct().getId(),
                        item.getVariant() != null ? item.getVariant().getId() : null,
                        item.getQuantity(), item.getAddedAt()))
                .toList();
        return new CartResponse(cart.getId(), cart.getUser() != null ? cart.getUser().getId() : null,
                cart.getGuestToken(), cart.getStatus(), items);
    }
}
