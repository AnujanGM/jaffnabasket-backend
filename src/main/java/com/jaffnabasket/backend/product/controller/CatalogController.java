package com.jaffnabasket.backend.product.controller;

import com.jaffnabasket.backend.product.dto.*;
import com.jaffnabasket.backend.product.entity.Locale;
import com.jaffnabasket.backend.product.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Catalog", description = "Public, unauthenticated product browsing")
@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @Operation(
            summary = "List products",
            description = "Returns a cursor-paginated page of ACTIVE products. Optional filters: `category`/`brand` "
                    + "(match by slug, not id), `attributes` (format: `key:value,key:value`, e.g. `color:red,size:M`), "
                    + "and `locale` (EN/TA/SI, defaults to EN) which selects which translation's name/description is "
                    + "returned per item. To get the next page, pass the `nextCursor` from this response back in as "
                    + "`cursor`; leave `cursor` empty for the first page. Public endpoint, no authentication required."
    )
    @GetMapping("/products")
    public CursorPageResponse<ProductSummaryResponse> listProducts(
            @Parameter(description = "Category slug to filter by, e.g. \"groceries\"") @RequestParam(required = false) String category,
            @Parameter(description = "Brand slug to filter by") @RequestParam(required = false) String brand,
            @Parameter(description = "Attribute filters as key:value pairs, comma-separated, e.g. \"color:red,size:M\"") @RequestParam(required = false) String attributes,
            @Parameter(description = "Locale for the returned name/description; defaults to EN") @RequestParam(required = false) Locale locale,
            @Parameter(description = "Opaque cursor from a previous response's nextCursor; omit for the first page") @RequestParam(required = false) String cursor,
            @Parameter(description = "Page size, default 20, max 100") @RequestParam(required = false) Integer size) {
        return catalogService.listProducts(category, brand, attributes, locale, cursor, size);
    }

    @Operation(
            summary = "Get product detail by slug",
            description = "Returns full product detail (translation, media, variants, attributes) for the given slug, "
                    + "in the requested locale. If no translation exists for that locale, falls back to EN."
    )
    @GetMapping("/products/{slug}")
    public ProductDetailResponse getProduct(
            @Parameter(description = "URL-friendly product slug, e.g. \"sample-product\"") @PathVariable String slug,
            @Parameter(description = "Preferred locale (EN/TA/SI); falls back to EN if missing") @RequestParam(required = false) Locale locale) {
        return catalogService.getBySlug(slug, locale);
    }

    @Operation(summary = "List categories", description = "Returns every product category, flat (not nested), including each one's parentId if it has a parent.")
    @GetMapping("/categories")
    public List<CategoryResponse> listCategories() {
        return catalogService.listCategories();
    }

    @Operation(summary = "List brands", description = "Returns every product brand.")
    @GetMapping("/brands")
    public List<BrandResponse> listBrands() {
        return catalogService.listBrands();
    }
}
