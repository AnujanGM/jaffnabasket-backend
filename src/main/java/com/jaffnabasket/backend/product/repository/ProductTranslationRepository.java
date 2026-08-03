package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.Locale;
import com.jaffnabasket.backend.product.entity.ProductTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductTranslationRepository extends JpaRepository<ProductTranslation, UUID> {

    Optional<ProductTranslation> findByProduct_IdAndLocale(UUID productId, Locale locale);

    List<ProductTranslation> findByProduct_Id(UUID productId);
}
