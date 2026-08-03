package com.jaffnabasket.backend.product.controller;

import com.jaffnabasket.backend.product.dto.AdminProductResponse;
import com.jaffnabasket.backend.product.dto.ProductCreateRequest;
import com.jaffnabasket.backend.product.dto.ProductUpdateRequest;
import com.jaffnabasket.backend.product.service.ProductAdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasAnyRole('CATALOG_MANAGER','SUPER_ADMIN')")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductAdminService productAdminService;

    @PostMapping
    public ResponseEntity<AdminProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productAdminService.create(request));
    }

    @PutMapping("/{id}")
    public AdminProductResponse update(@PathVariable UUID id, @Valid @RequestBody ProductUpdateRequest request) {
        return productAdminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        productAdminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
