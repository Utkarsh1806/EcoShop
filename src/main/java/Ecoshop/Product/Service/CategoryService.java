package Ecoshop.Product.Service;

import Ecoshop.Exceptions.ResourceAlreadyExistException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.*;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CategoryService {

    CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO) throws ResourceAlreadyExistException, ResourceNotFoundException;

    CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) throws ResourceNotFoundException;

    PagedResponse<CategoryResponseDTO> getAll(int page, int size);

    CategoryResponseDTO getCategory(Long id) throws ResourceNotFoundException;

    CategoryResponseDTO delete(Long id) throws ResourceNotFoundException;
}
