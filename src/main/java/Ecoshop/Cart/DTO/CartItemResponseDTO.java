package Ecoshop.Cart.DTO;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDTO {
    private Long productId;
    private String productName;
    private String productImage;
    private Double unitPrice;
    private Integer quantity;
    private Double totalPrice;
}
