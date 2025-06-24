package Ecoshop.Product.Service;

import Ecoshop.Exceptions.ResourceAlreadyExistException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.BrandRequestDTO;
import Ecoshop.Product.DTO.BrandResponseDTO;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.DTO.ProductResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;


public interface BrandService {

    BrandResponseDTO create(BrandRequestDTO brandRequestDTO) throws ResourceAlreadyExistException, ResourceNotFoundException;

    BrandResponseDTO updateBrand(Long id ,BrandRequestDTO brandRequestDTO) throws ResourceNotFoundException, ResourceAlreadyExistException;

    PagedResponse<BrandResponseDTO> getAll(int page, int size);

    BrandResponseDTO getBrand(Long id) throws ResourceNotFoundException;

    BrandResponseDTO delete(Long id) throws ResourceNotFoundException;
}
