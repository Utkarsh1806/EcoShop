package Ecoshop.Product.Controller;

import Ecoshop.Product.DTO.ProductRequestDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    ProductService productService;

    ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO product) {
        Product product1 = new Product();
        product1.setName(product.getName());
        return ResponseEntity.ok(productService.addProduct(product));
    }

    @GetMapping("{/id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("{/category")
    public ResponseEntity<List<ProductResponseDTO>> getProductByCategory(@PathVariable String category) {
        return ResponseEntity.ok(productService.getProductsByCategory(category));
    }

    @GetMapping("{/brand")
    public ResponseEntity<List<ProductResponseDTO>> getProductByBrand(@PathVariable String brand) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand));
    }

    @GetMapping("{/Vegeterian}")
    public ResponseEntity<List<ProductResponseDTO>> getProductByVegeterian(@PathVariable Boolean vegeterian) {
        return ResponseEntity.ok(productService.getProductsByIsVegetarian(vegeterian));
    }

    @GetMapping("{/Returnable}")
    public ResponseEntity<List<ProductResponseDTO>> getProductByReturnable(@PathVariable Boolean returnable) {
        return ResponseEntity.ok(productService.getProductsByIsReturnable(returnable));
    }

    @PutMapping("{/id")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDTO product) {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }
}
