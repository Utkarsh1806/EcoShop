package Ecoshop.Product.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String iconUrl;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory", cascade = CascadeType.MERGE)
    private Set<Category> subCategories;

    @OneToMany(mappedBy = "category", cascade = CascadeType.MERGE)
    private Set<Product> products;
}
