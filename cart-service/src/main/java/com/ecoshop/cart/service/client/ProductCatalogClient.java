package com.ecoshop.cart.service.client;

import com.ecoshop.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "product-catalog-service", path = "/api/products")
public interface ProductCatalogClient {

    @GetMapping("/{id}")
    ApiResponse<ProductView> getById(@PathVariable("id") UUID id);

    record ProductView(
            UUID id,
            String name,
            String slug,
            String shortDescription,
            UUID categoryId,
            UUID brandId,
            BigDecimal basePrice,
            String currency,
            String status,
            BigDecimal ratingAvg,
            Integer ratingCount,
            List<VariantView> variants,
            List<ImageView> images
    ) {}

    record VariantView(
            UUID id, String sku, String size, String color, String storage,
            BigDecimal price, BigDecimal compareAtPrice, boolean active
    ) {}

    record ImageView(
            UUID id, String url, String altText, Integer displayOrder
    ) {}
}
