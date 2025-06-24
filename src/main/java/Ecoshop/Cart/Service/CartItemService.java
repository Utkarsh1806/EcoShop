package Ecoshop.Cart.Service;

import Ecoshop.Cart.DTO.CartItemRequestDTO;
import Ecoshop.Cart.DTO.CartItemResponseDTO;
import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Exceptions.ResourceNotFoundException;

public interface CartItemService {
    CartItemResponseDTO addItemToCart(Long cartId, CartItemRequestDTO itemRequestDTO) throws ResourceNotFoundException;

    CartItemResponseDTO updateCartItemQuantity(Long cartItemId, Long productId, int quantity) throws ResourceNotFoundException;

    CartItemResponseDTO removeItemFromCart(Long cartId, Long cartItemId) throws ResourceNotFoundException;
}
