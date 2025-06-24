package Ecoshop.Product.Repository;

import Ecoshop.Product.Entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameAndBrandIdAndCategoryId(String name, Long brandId, Long categoryId);
    Page<Product> findByBrand_NameIgnoreCase(String brandName, Pageable pageable);
    Page<Product> findByCategory_NameIgnoreCase(String category, Pageable pageable);
    Page<Product> findByIsReturnable(Pageable pageable, Boolean isReturnable);
    Page<Product> findByIsVegetarian(Pageable pageable, Boolean isVegetarian);
    boolean existsByNameAndBrandIdAndCategoryIdAndIdNot(String name, Long brandId, Long categoryId, Long id);
}
