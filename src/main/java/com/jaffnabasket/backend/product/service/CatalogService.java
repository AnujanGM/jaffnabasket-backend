package com.jaffnabasket.backend.product.service;

import com.jaffnabasket.backend.exception.ResourceNotFoundException;
import com.jaffnabasket.backend.product.dto.*;
import com.jaffnabasket.backend.product.entity.*;
import com.jaffnabasket.backend.product.pagination.CursorCodec;
import com.jaffnabasket.backend.product.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final ProductRepository productRepository;
    private final ProductTranslationRepository productTranslationRepository;
    private final ProductMediaRepository productMediaRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductAttributeRepository productAttributeRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public CursorPageResponse<ProductSummaryResponse> listProducts(String categorySlug, String brandSlug,
                                                                     String attributesParam, Locale locale,
                                                                     String cursor, Integer size) {
        int pageSize = clampPageSize(size);
        Locale effectiveLocale = locale != null ? locale : Locale.EN;

        Specification<Product> spec = Specification.where(ProductSpecifications.statusIs(ProductStatus.ACTIVE))
                .and(ProductSpecifications.hasCategorySlug(categorySlug))
                .and(ProductSpecifications.hasBrandSlug(brandSlug));

        for (Map.Entry<String, String> attr : parseAttributes(attributesParam).entrySet()) {
            spec = spec.and(ProductSpecifications.hasAttribute(attr.getKey(), attr.getValue()));
        }
        if (cursor != null && !cursor.isBlank()) {
            CursorCodec.CursorPosition position = CursorCodec.decode(cursor);
            spec = spec.and(ProductSpecifications.keysetBefore(position.createdAt(), position.id()));
        }

        Pageable pageable = PageRequest.of(0, pageSize + 1, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")));
        List<Product> products = productRepository.findAll(spec, pageable).getContent();

        boolean hasNext = products.size() > pageSize;
        List<Product> pageItems = hasNext ? products.subList(0, pageSize) : products;

        List<ProductSummaryResponse> items = pageItems.stream()
                .map(product -> toSummary(product, effectiveLocale))
                .toList();

        String nextCursor = hasNext
                ? CursorCodec.encode(pageItems.get(pageItems.size() - 1).getCreatedAt(), pageItems.get(pageItems.size() - 1).getId())
                : null;

        return new CursorPageResponse<>(items, nextCursor, hasNext);
    }

    @Transactional(readOnly = true)
    public ProductDetailResponse getBySlug(String slug, Locale locale) {
        Product product = productRepository.findBySlug(slug)
                .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Locale requested = locale != null ? locale : Locale.EN;
        ProductTranslation translation = resolveTranslation(product.getId(), requested);

        List<ProductMedia> media = productMediaRepository.findByProduct_IdOrderBySortOrderAsc(product.getId());
        List<ProductVariant> variants = productVariantRepository.findByProduct_Id(product.getId());
        List<ProductAttribute> attributes = productAttributeRepository.findByProduct_Id(product.getId());

        return ProductMapper.toDetailResponse(product, translation, media, variants, attributes);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<BrandResponse> listBrands() {
        return brandRepository.findAll().stream().map(ProductMapper::toResponse).toList();
    }

    private ProductTranslation resolveTranslation(UUID productId, Locale requested) {
        return productTranslationRepository.findByProduct_IdAndLocale(productId, requested)
                .or(() -> productTranslationRepository.findByProduct_IdAndLocale(productId, Locale.EN))
                .orElseThrow(() -> new ResourceNotFoundException("No translation available for product"));
    }

    private ProductSummaryResponse toSummary(Product product, Locale locale) {
        ProductTranslation translation = productTranslationRepository.findByProduct_IdAndLocale(product.getId(), locale)
                .or(() -> productTranslationRepository.findByProduct_IdAndLocale(product.getId(), Locale.EN))
                .orElse(null);
        String primaryImageUrl = productMediaRepository.findByProduct_IdOrderBySortOrderAsc(product.getId()).stream()
                .findFirst().map(ProductMedia::getUrl).orElse(null);
        return ProductMapper.toSummaryResponse(product, translation, primaryImageUrl);
    }

    private Map<String, String> parseAttributes(String attributesParam) {
        if (attributesParam == null || attributesParam.isBlank()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (String pair : attributesParam.split(",")) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                result.put(kv[0].trim(), kv[1].trim());
            }
        }
        return result;
    }

    private int clampPageSize(Integer size) {
        if (size == null) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.max(1, Math.min(size, MAX_PAGE_SIZE));
    }
}
