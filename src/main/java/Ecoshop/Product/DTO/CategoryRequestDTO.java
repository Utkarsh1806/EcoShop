package Ecoshop.Product.DTO;

import lombok.*;

import java.util.Set;

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
    private Set<Long> subCategoriesId;
    private Set<Long> productCategoryIds;
}
