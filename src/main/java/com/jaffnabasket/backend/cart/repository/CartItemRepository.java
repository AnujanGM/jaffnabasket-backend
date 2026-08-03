package com.jaffnabasket.backend.cart.repository;

import com.jaffnabasket.backend.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

    List<CartItem> findByCart_Id(UUID cartId);

    Optional<CartItem> findByCart_IdAndProduct_IdAndVariantIsNull(UUID cartId, UUID productId);

    Optional<CartItem> findByCart_IdAndProduct_IdAndVariant_Id(UUID cartId, UUID productId, UUID variantId);
}
