package com.jaffnabasket.backend.product.service;

import com.jaffnabasket.backend.product.dto.*;
import com.jaffnabasket.backend.product.entity.*;

import java.util.List;

final class ProductMapper {

    private ProductMapper() {
    }

    static ProductSummaryResponse toSummaryResponse(Product product, ProductTranslation translation, String primaryImageUrl) {
        return new ProductSummaryResponse(
                product.getId(), product.getSku(), product.getSlug(), product.getType(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getBrand() != null ? product.getBrand().getSlug() : null,
                translation != null ? translation.getName() : null,
                translation != null ? translation.getShortDescription() : null,
                primaryImageUrl
        );
    }

    static ProductDetailResponse toDetailResponse(Product product, ProductTranslation translation,
                                                    List<ProductMedia> media, List<ProductVariant> variants,
                                                    List<ProductAttribute> attributes) {
        return new ProductDetailResponse(
                product.getId(), product.getSku(), product.getSlug(), product.getType(), product.getStatus(),
                product.getBrand() != null ? product.getBrand().getName() : null,
                product.getBrand() != null ? product.getBrand().getSlug() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getCategory() != null ? product.getCategory().getSlug() : null,
                product.isExportable(), product.isReturnable(),
                product.getWeight(), product.getDimensions(),
                translation.getLocale().name(),
                translation.getName(), translation.getShortDescription(), translation.getLongDescription(),
                translation.getSeoTitle(), translation.getSeoDescription(),
                media.stream().map(m -> new ProductMediaResponse(m.getId(), m.getUrl(), m.getAltText(), m.getSortOrder(), m.getType())).toList(),
                variants.stream().map(v -> new ProductVariantResponse(v.getId(), v.getSku(), v.getAttributes(), v.getStockPolicy())).toList(),
                attributes.stream().map(a -> new ProductAttributeResponse(a.getAttributeKey(), a.getAttributeValue())).toList(),
                product.getCreatedAt(), product.getUpdatedAt()
        );
    }

    static AdminProductResponse toAdminResponse(Product product) {
        return new AdminProductResponse(
                product.getId(), product.getSku(), product.getSlug(), product.getType(), product.getStatus(),
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getBrand() != null ? product.getBrand().getId() : null,
                product.isExportable(), product.isReturnable(), product.getWeight(), product.getDimensions(),
                product.getCreatedAt(), product.getUpdatedAt()
        );
    }

    static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(),
                category.getParent() != null ? category.getParent().getId() : null);
    }

    static BrandResponse toResponse(Brand brand) {
        return new BrandResponse(brand.getId(), brand.getName(), brand.getSlug());
    }
}
