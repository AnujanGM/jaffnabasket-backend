package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.ProductMedia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductMediaRepository extends JpaRepository<ProductMedia, UUID> {

    List<ProductMedia> findByProduct_IdOrderBySortOrderAsc(UUID productId);
}
