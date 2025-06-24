package Ecoshop.Cart.Controllers;


import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Cart.Service.CartService;
import Ecoshop.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }
    @GetMapping("/{userId}")
    public ResponseEntity<CartResponseDTO> getCartByUserId(@PathVariable Long userId) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartService.getCartByUserId(userId));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<CartResponseDTO> createCart(@PathVariable Long userId) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartService.createCartForUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) throws ResourceNotFoundException {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

}
