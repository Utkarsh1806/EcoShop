package Ecoshop.Product.Controller;

import Ecoshop.Exceptions.ProductDoesNotExistsException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.DTO.ProductRequestDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Service.ProductService;
import org.springframework.data.domain.Page;
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
    public ResponseEntity<ProductResponseDTO> addProduct(@RequestBody ProductRequestDTO product) throws ResourceNotFoundException {
        return ResponseEntity.ok(productService.addProduct(product));
    }

    @GetMapping("{id}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long id) throws ProductDoesNotExistsException {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping("/{category}")
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getProductByCategory(@PathVariable String category,
                                                                                  @RequestParam(defaultValue = "0")int page,
                                                                                  @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(category,page,size));
    }

    @GetMapping("/{brand}")
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getProductByBrand(@PathVariable String brand,
                                                                               @RequestParam(defaultValue = "0") int page,
                                                                               @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProductsByBrand(brand,page,size));
    }

    @GetMapping("/{Vegeterian}")
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getProductByVegeterian(@PathVariable Boolean vegeterian,
                                                                           @RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "0") int size) {
        return ResponseEntity.ok(productService.getProductsByIsVegetarian(vegeterian,page,size));
    }

    @GetMapping("/{Returnable}")
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getProductByReturnable(@PathVariable Boolean returnable,
                                                                                    @RequestParam(defaultValue = "0") int page,
                                                                                    @RequestParam(defaultValue = "0") int size) {
        return ResponseEntity.ok(productService.getProductsByIsReturnable(returnable,page,size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(@PathVariable Long id, @RequestBody ProductRequestDTO product) throws ProductDoesNotExistsException, ResourceNotFoundException {
        return ResponseEntity.ok(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> deleteProduct(@PathVariable Long id) throws ProductDoesNotExistsException {
        return ResponseEntity.ok(productService.deleteProduct(id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponseDTO>> getProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(productService.getProducts(page, size));
    }
}
