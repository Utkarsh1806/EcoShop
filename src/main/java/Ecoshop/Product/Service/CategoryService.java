package Ecoshop.Product.Service;

import Ecoshop.Product.DTO.BrandRequestDTO;
import Ecoshop.Product.DTO.BrandResponseDTO;
import Ecoshop.Product.DTO.CategoryRequestDTO;
import Ecoshop.Product.DTO.CategoryResponseDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CategoryService {

    CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO);

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO);

    List<CategoryResponseDTO> getAll();

    CategoryResponseDTO getCategory(Long id);

    CategoryResponseDTO delete(Long id);
}
