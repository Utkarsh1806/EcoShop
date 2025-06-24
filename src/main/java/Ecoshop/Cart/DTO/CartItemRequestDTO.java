package Ecoshop.Cart.DTO;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequestDTO {
    private Long productId;
    private Integer quantity;
}
