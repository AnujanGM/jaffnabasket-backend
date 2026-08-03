package com.jaffnabasket.backend.product.service;

import com.jaffnabasket.backend.exception.ConflictException;
import com.jaffnabasket.backend.exception.ResourceNotFoundException;
import com.jaffnabasket.backend.product.dto.AdminProductResponse;
import com.jaffnabasket.backend.product.dto.ProductCreateRequest;
import com.jaffnabasket.backend.product.dto.ProductTranslationRequest;
import com.jaffnabasket.backend.product.dto.ProductUpdateRequest;
import com.jaffnabasket.backend.product.entity.Brand;
import com.jaffnabasket.backend.product.entity.Category;
import com.jaffnabasket.backend.product.entity.Product;
import com.jaffnabasket.backend.product.entity.ProductTranslation;
import com.jaffnabasket.backend.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductAdminService {

    private final ProductRepository productRepository;
    private final ProductTranslationRepository productTranslationRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final ProductRelationRepository productRelationRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Transactional
    public AdminProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsBySlug(request.slug())) {
            throw new ConflictException("Slug already in use");
        }

        Category category = resolveCategory(request.categoryId());
        Brand brand = resolveBrand(request.brandId());

        Product product = Product.builder()
                .sku(request.sku())
                .slug(request.slug())
                .type(request.type())
                .status(request.status())
                .category(category)
                .brand(brand)
                .exportable(request.exportable())
                .returnable(request.returnable())
                .weight(request.weight())
                .dimensions(request.dimensions())
                .build();
        product = productRepository.save(product);

        for (ProductTranslationRequest t : request.translations()) {
            productTranslationRepository.save(ProductTranslation.builder()
                    .product(product)
                    .locale(t.locale())
                    .name(t.name())
                    .shortDescription(t.shortDescription())
                    .longDescription(t.longDescription())
                    .seoTitle(t.seoTitle())
                    .seoDescription(t.seoDescription())
                    .build());
        }

        return ProductMapper.toAdminResponse(product);
    }

    @Transactional
    public AdminProductResponse update(UUID id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (request.slug() != null && !request.slug().equals(product.getSlug())) {
            if (productRepository.existsBySlug(request.slug())) {
                throw new ConflictException("Slug already in use");
            }
            product.setSlug(request.slug());
        }
        if (request.status() != null) {
            product.setStatus(request.status());
        }
        if (request.categoryId() != null) {
            product.setCategory(resolveCategory(request.categoryId()));
        }
        if (request.brandId() != null) {
            product.setBrand(resolveBrand(request.brandId()));
        }
        if (request.exportable() != null) {
            product.setExportable(request.exportable());
        }
        if (request.returnable() != null) {
            product.setReturnable(request.returnable());
        }
        if (request.weight() != null) {
            product.setWeight(request.weight());
        }
        if (request.dimensions() != null) {
            product.setDimensions(request.dimensions());
        }
        productRepository.save(product);

        return ProductMapper.toAdminResponse(product);
    }

    @Transactional
    public void delete(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        productMediaRepository.deleteAll(productMediaRepository.findByProduct_IdOrderBySortOrderAsc(id));
        productAttributeRepository.deleteAll(productAttributeRepository.findByProduct_Id(id));
        productTranslationRepository.deleteAll(productTranslationRepository.findByProduct_Id(id));
        productVariantRepository.deleteAll(productVariantRepository.findByProduct_Id(id));
        productRelationRepository.deleteAll(productRelationRepository.findByProduct_Id(id));
        productRepository.delete(product);
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    private Brand resolveBrand(UUID brandId) {
        if (brandId == null) {
            return null;
        }
        return brandRepository.findById(brandId)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found"));
    }
}
