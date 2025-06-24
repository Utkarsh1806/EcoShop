package Ecoshop.Cart.Service.Impl;

import Ecoshop.Cart.DTO.CartItemRequestDTO;
import Ecoshop.Cart.DTO.CartItemResponseDTO;
import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Cart.Entity.Cart;
import Ecoshop.Cart.Entity.CartItem;
import Ecoshop.Cart.Mapper.CartItemMapper;
import Ecoshop.Cart.Repository.CartItemRepository;
import Ecoshop.Cart.Repository.CartRepository;
import Ecoshop.Cart.Service.CartItemService;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartItemServiceImpl implements CartItemService {

    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public CartItemServiceImpl(CartItemMapper cartItemMapper, CartItemRepository cartItemRepository, CartRepository cartRepository, ProductRepository productRepository) {
        this.cartItemMapper = cartItemMapper;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
    }

    public static final Logger log = LoggerFactory.getLogger(CartItemServiceImpl.class);

    @Override
    @Transactional
    public CartItemResponseDTO addItemToCart(Long cartId, CartItemRequestDTO itemRequestDTO) throws ResourceNotFoundException {
        log.info("Adding item to cart with ID: {}", cartId);

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found with ID: " + cartId));

        Product product = productRepository.findById(itemRequestDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemRequestDTO.getProductId()));

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(itemRequestDTO.getQuantity())
                .price(product.getPrice() * itemRequestDTO.getQuantity())
                .build();

        CartItem savedCartItem = cartItemRepository.save(cartItem);

        log.info("Item with product ID: {} added to cart ID: {}", product.getId(), cartId);
        return cartItemMapper.toDTO(savedCartItem);
    }

    @Override
    @Transactional
    public CartItemResponseDTO updateCartItemQuantity(Long cartItemId, Long productId, int quantity) throws ResourceNotFoundException {
        log.info("Updating quantity of CartItem ID: {} to {}", cartItemId, quantity);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found with ID: " + cartItemId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        cartItem.setProduct(product); // Optional, assuming product change is allowed
        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice() * quantity);

        CartItem updatedItem = cartItemRepository.save(cartItem);

        log.info("CartItem ID: {} updated successfully", cartItemId);
        return cartItemMapper.toDTO(updatedItem);
    }

    @Override
    @Transactional
    public CartItemResponseDTO removeItemFromCart(Long cartId, Long cartItemId) throws ResourceNotFoundException {
        log.info("Attempting to remove CartItem ID: {} from Cart ID: {}", cartItemId, cartId);

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("CartItem not found with ID: " + cartItemId));

        if (!cartItem.getCart().getId().equals(cartId)) {
            throw new ResourceNotFoundException("CartItem does not belong to Cart with ID: " + cartId);
        }
        CartItemResponseDTO responseDTO = cartItemMapper.toDTO(cartItem);
        cartItemRepository.delete(cartItem);
        log.info("CartItem ID: {} successfully removed from Cart ID: {}", cartItemId, cartId);
        return responseDTO;
    }
}
