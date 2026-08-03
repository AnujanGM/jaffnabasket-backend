package com.jaffnabasket.backend.cart.repository;

import com.jaffnabasket.backend.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {

    Optional<Cart> findByUser_Id(UUID userId);

    Optional<Cart> findByGuestToken(String guestToken);
}
