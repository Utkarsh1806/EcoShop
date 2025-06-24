package Ecoshop.Cart.Service;

import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Exceptions.ResourceNotFoundException;

public interface CartService {
    CartResponseDTO getCartByUserId(Long userId) throws ResourceNotFoundException;

    CartResponseDTO createCartForUser(Long userId) throws ResourceNotFoundException;

    void clearCart(Long userId) throws ResourceNotFoundException;

}
