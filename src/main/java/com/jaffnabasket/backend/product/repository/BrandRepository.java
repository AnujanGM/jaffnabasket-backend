package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {

    Optional<Brand> findBySlug(String slug);
}
