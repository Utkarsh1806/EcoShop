package Ecoshop.Product.DTO;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String iconUrl;
    private String description;
    private Long parentCategoryId;
    private String parentCategoryName;
    private Set<SubCategoryDTO> subCategories;
    private Set<ProductResponseDTO> products;
}
