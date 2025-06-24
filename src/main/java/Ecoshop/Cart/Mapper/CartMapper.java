package Ecoshop.Cart.Mapper;

import Ecoshop.Cart.DTO.CartResponseDTO;
import Ecoshop.Cart.Entity.Cart;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "items", target = "items")
    @Mapping(expression = "java(cart.getItems().stream().mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum())", target = "totalAmount")
    CartResponseDTO toDTO(Cart cart);
}
