package com.ecoshop.product.catalog.service.repo;

import com.ecoshop.product.catalog.service.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findBySlug(String slug);
    List<Category> findByParentId(UUID parentId);
    List<Category> findByActiveTrue();
    boolean existsBySlug(String slug);
}
