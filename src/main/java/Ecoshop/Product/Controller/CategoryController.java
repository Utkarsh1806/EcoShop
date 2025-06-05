package Ecoshop.Product.Controller;

import Ecoshop.Product.DTO.CategoryRequestDTO;
import Ecoshop.Product.DTO.CategoryResponseDTO;
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
    public ResponseEntity<CategoryResponseDTO> createCategory(@RequestBody CategoryRequestDTO categoryRequestDTO) {
        return ResponseEntity.ok(categoryService.create(categoryRequestDTO));
    }

    @PutMapping("{/id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO categoryRequestDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryRequestDTO));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    @GetMapping("{/id")
    public ResponseEntity<CategoryResponseDTO> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @DeleteMapping("{/id}")
    public ResponseEntity<CategoryResponseDTO> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.delete(id));
    }
}
