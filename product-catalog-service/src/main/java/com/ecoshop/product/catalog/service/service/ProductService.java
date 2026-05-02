package com.ecoshop.product.catalog.service.service;

import com.ecoshop.common.dto.PageResponse;
import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.product.catalog.service.domain.*;
import com.ecoshop.product.catalog.service.dto.CatalogDtos.*;
import com.ecoshop.product.catalog.service.repo.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductRequest req) {
        if (productRepository.existsBySlug(req.slug())) {
            throw BusinessException.conflict("PRODUCT_SLUG_EXISTS",
                    "Slug already in use: " + req.slug());
        }
        Product product = Product.builder()
                .name(req.name())
                .slug(req.slug())
                .description(req.description())
                .shortDescription(req.shortDescription())
                .categoryId(req.categoryId())
                .brandId(req.brandId())
                .basePrice(req.basePrice())
                .currency(req.currency() != null ? req.currency() : "INR")
                .status(req.status() != null ? req.status() : ProductStatus.DRAFT)
                .build();

        if (req.variants() != null) {
            for (VariantRequest vr : req.variants()) {
                product.addVariant(ProductVariant.builder()
                        .sku(vr.sku())
                        .size(vr.size())
                        .color(vr.color())
                        .storage(vr.storage())
                        .price(vr.price())
                        .compareAtPrice(vr.compareAtPrice())
                        .active(true)
                        .build());
            }
        }
        if (req.images() != null) {
            for (ImageRequest ir : req.images()) {
                product.addImage(ProductImage.builder()
                        .url(ir.url())
                        .altText(ir.altText())
                        .displayOrder(ir.displayOrder() != null ? ir.displayOrder() : 0)
                        .build());
            }
        }
        return toResponse(productRepository.save(product));
    }

    @Transactional(readOnly = true)
    public ProductResponse getById(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND",
                        "Product " + id + " not found"));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySlug(String slug) {
        Product p = productRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND",
                        "No product with slug " + slug));
        return toResponse(p);
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductSummary> list(Optional<UUID> categoryId, Optional<String> q,
                                             int page, int size, String sortBy) {
        Sort sort = Sort.by(parseSort(sortBy));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> result;
        if (q.isPresent() && !q.get().isBlank()) {
            result = productRepository.searchByText(q.get(), ProductStatus.ACTIVE, pageable);
        } else if (categoryId.isPresent()) {
            result = productRepository.findByCategoryIdAndStatus(categoryId.get(), ProductStatus.ACTIVE, pageable);
        } else {
            result = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        }

        List<ProductSummary> summaries = result.getContent().stream().map(this::toSummary).toList();
        return new PageResponse<>(summaries, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public ProductResponse update(UUID id, ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND",
                        "Product " + id + " not found"));
        p.setName(req.name());
        p.setDescription(req.description());
        p.setShortDescription(req.shortDescription());
        p.setCategoryId(req.categoryId());
        p.setBrandId(req.brandId());
        p.setBasePrice(req.basePrice());
        if (req.currency() != null) p.setCurrency(req.currency());
        if (req.status() != null) p.setStatus(req.status());
        return toResponse(p);
    }

    @Transactional
    public void delete(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("PRODUCT_NOT_FOUND",
                        "Product " + id + " not found"));
        p.setStatus(ProductStatus.ARCHIVED);
    }

    private Sort.Order parseSort(String sortBy) {
        return switch (sortBy != null ? sortBy.toLowerCase() : "created_desc") {
            case "price_asc" -> Sort.Order.asc("basePrice");
            case "price_desc" -> Sort.Order.desc("basePrice");
            case "rating_desc" -> Sort.Order.desc("ratingAvg");
            case "name_asc" -> Sort.Order.asc("name");
            default -> Sort.Order.desc("createdAt");
        };
    }

    public ProductSummary toSummary(Product p) {
        String thumb = p.getImages().isEmpty() ? null : p.getImages().get(0).getUrl();
        return new ProductSummary(
                p.getId(), p.getName(), p.getSlug(), p.getShortDescription(),
                p.getBasePrice(), p.getCurrency(), p.getStatus(),
                p.getRatingAvg(), p.getRatingCount(), thumb
        );
    }

    public ProductResponse toResponse(Product p) {
        List<VariantResponse> variants = p.getVariants().stream()
                .map(v -> new VariantResponse(v.getId(), v.getSku(), v.getSize(),
                        v.getColor(), v.getStorage(), v.getPrice(), v.getCompareAtPrice(), v.isActive()))
                .toList();
        List<ImageResponse> images = p.getImages().stream()
                .map(i -> new ImageResponse(i.getId(), i.getUrl(), i.getAltText(), i.getDisplayOrder()))
                .toList();
        return new ProductResponse(
                p.getId(), p.getName(), p.getSlug(), p.getDescription(), p.getShortDescription(),
                p.getCategoryId(), p.getBrandId(), p.getBasePrice(), p.getCurrency(),
                p.getStatus(), p.getRatingAvg(), p.getRatingCount(), variants, images
        );
    }
}
