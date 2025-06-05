package Ecoshop.Product.Service;

import Ecoshop.Product.DTO.ProductRequestDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Brand;
import Ecoshop.Product.Entity.Category;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ProductService {
    ProductResponseDTO addProduct(ProductRequestDTO product);

    List<ProductResponseDTO> getProducts();
    List<ProductResponseDTO> getProductsByBrand(String brand);
    List<ProductResponseDTO> getProductsByIsReturnable(Boolean isReturnable);
    List<ProductResponseDTO> getProductsByIsVegetarian(Boolean isVegetarian);
    List<ProductResponseDTO> getProductsByCategory(String category);

    ProductResponseDTO getProduct(Long id);

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO product);

    ProductResponseDTO deleteProduct(Long id);

}
