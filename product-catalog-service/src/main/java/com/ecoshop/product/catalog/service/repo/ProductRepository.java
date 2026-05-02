package com.ecoshop.product.catalog.service.repo;

import com.ecoshop.product.catalog.service.domain.Product;
import com.ecoshop.product.catalog.service.domain.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

    Optional<Product> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Page<Product> findByStatus(ProductStatus status, Pageable pageable);
    Page<Product> findByCategoryIdAndStatus(UUID categoryId, ProductStatus status, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.status = :status " +
           "AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Product> searchByText(String q, ProductStatus status, Pageable pageable);
}
