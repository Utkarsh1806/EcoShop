package com.ecoshop.product.catalog.service.api;

import com.ecoshop.common.dto.ApiResponse;
import com.ecoshop.product.catalog.service.dto.CatalogDtos.*;
import com.ecoshop.product.catalog.service.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ApiResponse<List<BrandResponse>> list() {
        return ApiResponse.ok(brandService.listAll());
    }

    @PostMapping
    public ApiResponse<BrandResponse> create(@Valid @RequestBody BrandRequest req) {
        return ApiResponse.ok(brandService.create(req));
    }

    @PutMapping("/{id}")
    public ApiResponse<BrandResponse> update(@PathVariable UUID id,
                                             @Valid @RequestBody BrandRequest req) {
        return ApiResponse.ok(brandService.update(id, req));
    }
}
