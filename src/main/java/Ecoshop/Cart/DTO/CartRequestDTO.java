package Ecoshop.Cart.DTO;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequestDTO {
    private Long userId;
    private Set<Long> cartItemsId;
}
