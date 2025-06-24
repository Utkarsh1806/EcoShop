package Ecoshop.Product.Mapper;

import Ecoshop.Product.DTO.ProductRequestDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Product;
import org.mapstruct.Mapping;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "brand.id", target = "brandId")
    @Mapping(source = "brand.name", target = "brandName")
    ProductResponseDTO toDTO(Product product);

    @Mapping(source = "categoryId", target = "category.id")
    @Mapping(source = "brandId", target = "brand.id")
    @Mapping(target = "id", ignore = true)
    Product toEntity(ProductRequestDTO dto);
}
