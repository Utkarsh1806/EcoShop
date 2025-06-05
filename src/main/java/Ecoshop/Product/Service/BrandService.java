package Ecoshop.Product.Service;

import Ecoshop.Product.DTO.BrandRequestDTO;
import Ecoshop.Product.DTO.BrandResponseDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Service
public interface BrandService {

    BrandResponseDTO create(BrandRequestDTO brandRequestDTO);

    BrandResponseDTO updateBrand(Long id ,BrandRequestDTO brandRequestDTO);

    List<BrandResponseDTO> getAll();

    BrandResponseDTO getBrand(Long id);

    BrandResponseDTO delete(Long id);
}
