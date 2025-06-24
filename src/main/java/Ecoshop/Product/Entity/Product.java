package Ecoshop.Product.Entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Accessors(chain = true)
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Double price;

    private Double discountPrice;

    @Column(nullable = false)
    private String unit;

    private Integer stockQuantity;

    private Boolean inStock;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    private Boolean isReturnable;

    private Boolean isVegetarian;

}
