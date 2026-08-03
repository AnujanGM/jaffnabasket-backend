package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, UUID> {

    List<ProductAttribute> findByProduct_Id(UUID productId);
}
