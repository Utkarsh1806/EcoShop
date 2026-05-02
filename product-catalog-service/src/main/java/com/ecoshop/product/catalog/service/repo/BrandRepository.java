package com.ecoshop.product.catalog.service.repo;

import com.ecoshop.product.catalog.service.domain.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BrandRepository extends JpaRepository<Brand, UUID> {
    Optional<Brand> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
