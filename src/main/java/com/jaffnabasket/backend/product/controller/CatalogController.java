package com.jaffnabasket.backend.product.controller;

import com.jaffnabasket.backend.product.dto.*;
import com.jaffnabasket.backend.product.entity.Locale;
import com.jaffnabasket.backend.product.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/products")
    public CursorPageResponse<ProductSummaryResponse> listProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String attributes,
            @RequestParam(required = false) Locale locale,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer size) {
        return catalogService.listProducts(category, brand, attributes, locale, cursor, size);
    }

    @GetMapping("/products/{slug}")
    public ProductDetailResponse getProduct(@PathVariable String slug,
                                             @RequestParam(required = false) Locale locale) {
        return catalogService.getBySlug(slug, locale);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> listCategories() {
        return catalogService.listCategories();
    }

    @GetMapping("/brands")
    public List<BrandResponse> listBrands() {
        return catalogService.listBrands();
    }
}
