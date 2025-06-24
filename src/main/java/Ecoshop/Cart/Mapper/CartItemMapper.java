package Ecoshop.Cart.Mapper;

import Ecoshop.Cart.DTO.CartItemRequestDTO;
import Ecoshop.Cart.DTO.CartItemResponseDTO;
import Ecoshop.Cart.Entity.CartItem;
import Ecoshop.Product.Entity.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "product.id", source = "productId")
    CartItem toEntity(CartItemRequestDTO dto);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.imageUrl", target = "productImage")
    @Mapping(source = "product.price", target = "unitPrice")
    @Mapping(expression = "java(cartItem.getQuantity() * cartItem.getProduct().getPrice())", target = "totalPrice")
    CartItemResponseDTO toDTO(CartItem cartItem);
}
