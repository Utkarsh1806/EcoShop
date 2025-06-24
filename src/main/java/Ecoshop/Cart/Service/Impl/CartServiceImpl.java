package Ecoshop.Cart.Service.Impl;

import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Cart.Entity.Cart;
import Ecoshop.Cart.Mapper.CartMapper;
import Ecoshop.Cart.Repository.CartRepository;
import Ecoshop.Cart.Service.CartService;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.User.Entity.User;
import Ecoshop.User.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, UserRepository userRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.cartMapper = cartMapper;
    }

    public static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    @Override
    @Transactional(readOnly = true)
    public CartResponseDTO getCartByUserId(Long userId) throws ResourceNotFoundException {
        log.info("Fetching cart for userId: {}", userId);
        Cart cart = userRepository.findById(userId)
                .map(User::getCart)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found for user with ID: " + userId);
        }

        return cartMapper.toDTO(cart);
    }

    @Override
    @Transactional
    public CartResponseDTO createCartForUser(Long userId) throws ResourceNotFoundException {
        log.info("Creating cart for userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (user.getCart() != null) {
            log.warn("Cart already exists for userId: {}", userId);
            return cartMapper.toDTO(user.getCart());
        }

        Cart cart = Cart.builder()
                .user(user)
                .items(new HashSet<>())
                .totalAmount(0.0)
                .build();

        Cart savedCart = cartRepository.save(cart);
        log.info("Cart created successfully for userId: {}", userId);

        return cartMapper.toDTO(savedCart);
    }

    @Override
    @Transactional
    public void clearCart(Long userId) throws ResourceNotFoundException {
        log.info("Clearing cart for userId: {}", userId);

        Cart cart = userRepository.findById(userId)
                .map(User::getCart)
                .orElseThrow(() -> new ResourceNotFoundException("User does not exist"));

        if (cart == null) {
            throw new ResourceNotFoundException("Cart not found for user with ID: " + userId);
        }

        cart.getItems().clear();
        cart.setTotalAmount(0.0);

        cartRepository.save(cart);
        log.info("Cart cleared for userId: {}", userId);
    }

}
