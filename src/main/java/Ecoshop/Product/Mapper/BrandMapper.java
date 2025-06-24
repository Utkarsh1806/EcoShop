package Ecoshop.Product.Mapper;

import Ecoshop.Product.DTO.*;
import Ecoshop.Product.Entity.*;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BrandMapper {

    @Mapping(source = "products", target = "products", qualifiedByName = "mapProducts")
    BrandResponseDTO toDTO(Brand brand);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    Brand toEntity(BrandRequestDTO dto);

    @Named("mapProducts")
    default Set<ProductResponseDTO> mapProducts(Set<Product> products) {
        if (products == null) return null;
        return products.stream()
                .map(product -> ProductResponseDTO.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .imageUrl(product.getImageUrl())
                        .build())
                .collect(Collectors.toSet());
    }
}