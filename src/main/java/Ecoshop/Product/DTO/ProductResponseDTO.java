package Ecoshop.Product.DTO;

import lombok.*;

import java.math.BigDecimal;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal discountPrice;
    private String unit;
    private Integer stockQuantity;
    private Boolean inStock;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long brandId;
    private String brandName;
    private Boolean isReturnable;
    private Boolean isVegetarian;
}