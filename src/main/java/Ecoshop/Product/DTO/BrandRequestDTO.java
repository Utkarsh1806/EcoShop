package Ecoshop.Product.DTO;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandRequestDTO {
    private String name;
    private String logoUrl;
    private String description;
}
