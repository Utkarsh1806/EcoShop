package com.ecoshop.product.catalog.service.dto;

import com.ecoshop.product.catalog.service.domain.ProductStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CatalogDtos {

    // ─── Categories ───
    public record CategoryRequest(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Size(max = 200) String slug,
            @Size(max = 1000) String description,
            UUID parentId,
            Integer displayOrder
    ) {}

    public record CategoryResponse(
            UUID id, String name, String slug, String description,
            UUID parentId, boolean active, Integer displayOrder
    ) {}

    // ─── Brands ───
    public record BrandRequest(
            @NotBlank @Size(max = 200) String name,
            @NotBlank @Size(max = 200) String slug,
            @Size(max = 500) String logoUrl,
            @Size(max = 1000) String description
    ) {}

    public record BrandResponse(
            UUID id, String name, String slug, String logoUrl, String description
    ) {}

    // ─── Variants ───
    public record VariantRequest(
            @NotBlank @Size(max = 100) String sku,
            String size,
            String color,
            String storage,
            @DecimalMin("0.0") BigDecimal price,
            @DecimalMin("0.0") BigDecimal compareAtPrice
    ) {}

    public record VariantResponse(
            UUID id, String sku, String size, String color, String storage,
            BigDecimal price, BigDecimal compareAtPrice, boolean active
    ) {}

    // ─── Images ───
    public record ImageRequest(
            @NotBlank String url,
            String altText,
            Integer displayOrder
    ) {}

    public record ImageResponse(
            UUID id, String url, String altText, Integer displayOrder
    ) {}

    // ─── Products ───
    public record ProductRequest(
            @NotBlank @Size(max = 300) String name,
            @NotBlank @Size(max = 300) String slug,
            String description,
            @Size(max = 500) String shortDescription,
            @NotNull UUID categoryId,
            UUID brandId,
            @NotNull @DecimalMin("0.0") BigDecimal basePrice,
            @Size(min = 3, max = 3) String currency,
            ProductStatus status,
            @Valid List<VariantRequest> variants,
            @Valid List<ImageRequest> images
    ) {}

    public record ProductSummary(
            UUID id, String name, String slug, String shortDescription,
            BigDecimal basePrice, String currency, ProductStatus status,
            BigDecimal ratingAvg, Integer ratingCount, String thumbnailUrl
    ) {}

    public record ProductResponse(
            UUID id, String name, String slug, String description, String shortDescription,
            UUID categoryId, UUID brandId, BigDecimal basePrice, String currency,
            ProductStatus status, BigDecimal ratingAvg, Integer ratingCount,
            List<VariantResponse> variants, List<ImageResponse> images
    ) {}
}
