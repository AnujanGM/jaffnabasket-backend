package com.jaffnabasket.backend.product.controller;

import com.jaffnabasket.backend.product.dto.AdminProductResponse;
import com.jaffnabasket.backend.product.dto.ProductCreateRequest;
import com.jaffnabasket.backend.product.dto.ProductUpdateRequest;
import com.jaffnabasket.backend.product.service.ProductAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Admin - Products", description = "Catalog management for CATALOG_MANAGER and SUPER_ADMIN roles")
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasAnyRole('CATALOG_MANAGER','SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductAdminService productAdminService;

    @Operation(
            summary = "Create a product",
            description = "Creates a new product along with at least one translation (translations cannot be empty - "
                    + "without one the product would have no displayable name). categoryId/brandId are optional but "
                    + "recommended, since they power catalog filtering. Requires CATALOG_MANAGER or SUPER_ADMIN."
    )
    @PostMapping
    public ResponseEntity<AdminProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productAdminService.create(request));
    }

    @Operation(
            summary = "Update a product",
            description = "Updates core fields on an existing product (slug, status, category, brand, exportable/"
                    + "returnable flags, weight, dimensions). Only the fields you include are changed; omit any field "
                    + "you don't want to touch. Does not manage translations, media, variants, or attributes. "
                    + "Requires CATALOG_MANAGER or SUPER_ADMIN."
    )
    @PutMapping("/{id}")
    public AdminProductResponse update(@Parameter(description = "UUID of the product to update") @PathVariable UUID id,
                                        @Valid @RequestBody ProductUpdateRequest request) {
        return productAdminService.update(id, request);
    }

    @Operation(
            summary = "Delete a product",
            description = "Permanently deletes a product and all of its translations, media, variants, attributes, and "
                    + "relations. This is a hard delete, not reversible. Requires CATALOG_MANAGER or SUPER_ADMIN."
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "UUID of the product to delete") @PathVariable UUID id) {
        productAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
