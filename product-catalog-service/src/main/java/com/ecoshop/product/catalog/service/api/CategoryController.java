package com.ecoshop.product.catalog.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.product.catalog.service.dto.CatalogDtos.*;
import com.ecoshop.product.catalog.service.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> list() {
        return ApiResponse.ok(categoryService.listAll());
    }

    @GetMapping("/slug/{slug}")
    public ApiResponse<CategoryResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.ok(categoryService.getBySlug(slug));
    }

    @PostMapping
    public ApiResponse<CategoryResponse> create(@Valid @RequestBody CategoryRequest req) {
        return ApiResponse.ok(categoryService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable UUID id,
                                                @Valid @RequestBody CategoryRequest req) {
        return ApiResponse.ok(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ApiResponse.ok(null);
    }
}
