package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    Optional<Category> findBySlug(String slug);
}
