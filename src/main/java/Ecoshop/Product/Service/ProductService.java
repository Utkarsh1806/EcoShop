package Ecoshop.Product.Service;

import Ecoshop.Exceptions.ProductDoesNotExistsException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.DTO.ProductRequestDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Brand;
import Ecoshop.Product.Entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

public interface ProductService {
    ProductResponseDTO addProduct(ProductRequestDTO product) throws ResourceNotFoundException;

    PagedResponse<ProductResponseDTO> getProducts(int size, int page);
    PagedResponse<ProductResponseDTO> getProductsByBrand(String brand, int page, int size);
    PagedResponse<ProductResponseDTO> getProductsByIsReturnable(Boolean isReturnable,int size, int page);
    PagedResponse<ProductResponseDTO> getProductsByIsVegetarian(Boolean isVegetarian, int size, int page);
    PagedResponse<ProductResponseDTO> getProductsByCategory(String category,int page, int size);

    ProductResponseDTO getProduct(Long id) throws ProductDoesNotExistsException;

    ProductResponseDTO updateProduct(Long id, ProductRequestDTO product) throws ProductDoesNotExistsException, ResourceNotFoundException;

    ProductResponseDTO deleteProduct(Long id) throws ProductDoesNotExistsException;

}
