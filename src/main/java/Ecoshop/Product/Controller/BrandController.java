package Ecoshop.Product.Controller;

import Ecoshop.Product.DTO.BrandRequestDTO;
import Ecoshop.Product.DTO.BrandResponseDTO;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Service.BrandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
public class BrandController {

    BrandService brandService;

    BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping
    public ResponseEntity<BrandResponseDTO> createBrand(@RequestBody BrandRequestDTO brandRequestDTO) {
        return ResponseEntity.ok(brandService.create(brandRequestDTO));
    }

    @PutMapping("{/id}")
    public ResponseEntity<BrandResponseDTO> updateBrand(@PathVariable Long id, @RequestBody BrandRequestDTO brandRequestDTO) {
        return ResponseEntity.ok(brandService.updateBrand(id, brandRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<BrandResponseDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAll());
    }

    @GetMapping("{/id")
    public ResponseEntity<BrandResponseDTO> getBrand(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrand(id));
    }

    @DeleteMapping("{/id}")
    public ResponseEntity<BrandResponseDTO> deleteBrand(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.delete(id));
    }
}
