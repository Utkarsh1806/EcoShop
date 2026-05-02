package com.ecoshop.product.catalog.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.product.catalog.service.dto.CatalogDtos.*;
import com.ecoshop.product.catalog.service.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<PageResponse<ProductSummary>> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "created_desc") String sort
    ) {
        return ApiResponse.ok(productService.list(
                Optional.ofNullable(categoryId), Optional.ofNullable(q), page, size, sort));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> getById(@PathVariable UUID id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<ProductResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(productService.getBySlug(slug));
    }

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok(productService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductResponse> update(@PathVariable UUID id,
                                               @Valid @RequestBody ProductRequest req) {
        return ApiResponse.ok(productService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        productService.delete(id);
        return ApiResponse.ok(null);
    }
}
