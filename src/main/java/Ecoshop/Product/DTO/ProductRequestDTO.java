package Ecoshop.Product.DTO;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
    public String name;
    private String description;
    private Double price;
    private Double discountPrice;
    private String unit;
    private Integer stockQuantity;
    private Boolean inStock;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private Boolean isReturnable;
    private Boolean isVegetarian;
}