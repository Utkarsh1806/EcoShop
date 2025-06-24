package Ecoshop.Product.DTO;

import lombok.*;

import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequestDTO {
    private String name;
    private String logoUrl;
    private String description;
    private Set<Long> productIds;
}
