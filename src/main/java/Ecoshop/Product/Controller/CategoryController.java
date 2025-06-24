package Ecoshop.Product.Controller;

import Ecoshop.Exceptions.ResourceAlreadyExistException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.CategoryRequestDTO;
import Ecoshop.Product.DTO.CategoryResponseDTO;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.Service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    
    CategoryService categoryService;
    
    CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) throws ResourceAlreadyExistException, ResourceNotFoundException {
        return ResponseEntity.ok(categoryService.create(categoryRequestDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO categoryRequestDTO) throws ResourceNotFoundException {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequestDTO));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<CategoryResponseDTO>> getAllCategories(@RequestParam int page, @RequestParam int size) {
        return ResponseEntity.ok(categoryService.getAll(page,size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> deleteCategory(@PathVariable Long id) throws ResourceNotFoundException {
        return ResponseEntity.ok(categoryService.delete(id));
    }
}
