package com.jaffnabasket.backend.product.repository;

import com.jaffnabasket.backend.product.entity.Product;
import com.jaffnabasket.backend.product.entity.ProductAttribute;
import com.jaffnabasket.backend.product.entity.ProductStatus;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.UUID;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> statusIs(ProductStatus status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Product> hasCategorySlug(String slug) {
        return (root, query, cb) -> (slug == null || slug.isBlank())
                ? null
                : cb.equal(root.get("category").get("slug"), slug);
    }

    public static Specification<Product> hasBrandSlug(String slug) {
        return (root, query, cb) -> (slug == null || slug.isBlank())
                ? null
                : cb.equal(root.get("brand").get("slug"), slug);
    }

    public static Specification<Product> hasAttribute(String key, String value) {
        return (root, query, cb) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            var attrRoot = subquery.from(ProductAttribute.class);
            subquery.select(attrRoot.get("product").get("id"))
                    .where(cb.equal(attrRoot.get("product").get("id"), root.get("id")),
                            cb.equal(attrRoot.get("attributeKey"), key),
                            cb.equal(attrRoot.get("attributeValue"), value));
            return cb.exists(subquery);
        };
    }

    public static Specification<Product> keysetBefore(Instant createdAt, UUID id) {
        return (root, query, cb) -> {
            if (createdAt == null || id == null) {
                return null;
            }
            return cb.or(
                    cb.lessThan(root.get("createdAt"), createdAt),
                    cb.and(cb.equal(root.get("createdAt"), createdAt), cb.lessThan(root.get("id"), id))
            );
        };
    }
}
