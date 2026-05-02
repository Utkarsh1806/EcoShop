package com.ecoshop.product.catalog.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.product.catalog.service.domain.Brand;
import com.ecoshop.product.catalog.service.dto.CatalogDtos.*;
import com.ecoshop.product.catalog.service.repo.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandResponse> listAll() {
        return brandRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public BrandResponse create(BrandRequest req) {
        if (brandRepository.existsBySlug(req.slug())) {
            throw BusinessException.conflict("BRAND_SLUG_EXISTS",
                    "Slug already in use: " + req.slug());
        }
        Brand b = Brand.builder()
                .name(req.name())
                .slug(req.slug())
                .logoUrl(req.logoUrl())
                .description(req.description())
                .build();
        return toResponse(brandRepository.save(b));
    }

    @Transactional
    public BrandResponse update(UUID id, BrandRequest req) {
        Brand b = brandRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("BRAND_NOT_FOUND",
                        "Brand " + id + " not found"));
        b.setName(req.name());
        b.setLogoUrl(req.logoUrl());
        b.setDescription(req.description());
        return toResponse(b);
    }

    public BrandResponse toResponse(Brand b) {
        return new BrandResponse(b.getId(), b.getName(), b.getSlug(), b.getLogoUrl(), b.getDescription());
    }
}
