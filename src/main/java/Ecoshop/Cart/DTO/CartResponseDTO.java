package Ecoshop.Cart.DTO;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponseDTO {
    private Long cartId;
    private Long userId;
    private Set<CartItemResponseDTO> items;
    private Double totalAmount;
}
