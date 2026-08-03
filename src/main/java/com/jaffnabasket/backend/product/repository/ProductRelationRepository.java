package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.ProductRelation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductRelationRepository extends JpaRepository<ProductRelation, UUID> {

    List<ProductRelation> findByProduct_Id(UUID productId);
}
