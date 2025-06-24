package Ecoshop.Product.Repository;

import Ecoshop.Product.Entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    boolean findByName(String name);
    boolean existsByName(String name);
}
