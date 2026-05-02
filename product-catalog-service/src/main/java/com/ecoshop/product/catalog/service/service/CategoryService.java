package com.ecoshop.product.catalog.service.service;

import com.ecoshop.common.exception.BusinessException;
import com.ecoshop.product.catalog.service.domain.Category;
import com.ecoshop.product.catalog.service.dto.CatalogDtos.*;
import com.ecoshop.product.catalog.service.repo.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> listAll() {
        return categoryRepository.findByActiveTrue().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CategoryResponse getBySlug(String slug) {
        Category c = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> BusinessException.notFound("CATEGORY_NOT_FOUND",
                        "No category with slug " + slug));
        return toResponse(c);
    }

    @Transactional
    public CategoryResponse create(CategoryRequest req) {
        if (categoryRepository.existsBySlug(req.slug())) {
            throw BusinessException.conflict("CATEGORY_SLUG_EXISTS",
                    "Slug already in use: " + req.slug());
        }
        Category c = Category.builder()
                .name(req.name())
                .slug(req.slug())
                .description(req.description())
                .parentId(req.parentId())
                .displayOrder(req.displayOrder() != null ? req.displayOrder() : 0)
                .active(true)
                .build();
        return toResponse(categoryRepository.save(c));
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CATEGORY_NOT_FOUND",
                        "Category " + id + " not found"));
        c.setName(req.name());
        c.setDescription(req.description());
        c.setParentId(req.parentId());
        if (req.displayOrder() != null) c.setDisplayOrder(req.displayOrder());
        return toResponse(c);
    }

    @Transactional
    public void delete(UUID id) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("CATEGORY_NOT_FOUND",
                        "Category " + id + " not found"));
        c.setActive(false); // soft delete
    }

    public CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(), c.getName(), c.getSlug(), c.getDescription(),
                c.getParentId(), c.isActive(), c.getDisplayOrder()
        );
    }
}
