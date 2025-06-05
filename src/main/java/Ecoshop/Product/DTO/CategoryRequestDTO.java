package Ecoshop.Product.DTO;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {
    private String name;
    private String iconUrl;
    private String description;
    private Long parentCategoryId;
}
