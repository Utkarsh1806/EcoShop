package Ecoshop.Cart.Controllers;

import Ecoshop.Cart.DTO.CartItemRequestDTO;
import Ecoshop.Cart.DTO.CartItemResponseDTO;
import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Cart.Service.CartItemService;
import Ecoshop.Exceptions.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart/{cartId}/items")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @PostMapping
    public ResponseEntity<CartItemResponseDTO> addItemToCart(
            @PathVariable Long cartId,
            @RequestBody CartItemRequestDTO itemRequestDTO) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartItemService.addItemToCart(cartId, itemRequestDTO));
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> updateItemQuantity(
            @PathVariable Long cartItemId,
            @RequestParam Long productId,
            @RequestParam int quantity) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartItemService.updateCartItemQuantity(cartItemId, productId, quantity));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<CartItemResponseDTO> removeItemFromCart(
            @PathVariable Long cartId,
            @PathVariable Long cartItemId) throws ResourceNotFoundException {
        return ResponseEntity.ok(cartItemService.removeItemFromCart(cartId, cartItemId));
    }
}

