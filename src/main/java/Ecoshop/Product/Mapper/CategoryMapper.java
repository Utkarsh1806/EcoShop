package Ecoshop.Product.Mapper;

import Ecoshop.Product.DTO.*;
import Ecoshop.Product.Entity.*;
import org.mapstruct.*;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentCategoryId", expression = "java(category.getParentCategory() != null ? category.getParentCategory().getId() : null)")
    @Mapping(target = "parentCategoryName", expression = "java(category.getParentCategory() != null ? category.getParentCategory().getName() : null)")
    @Mapping(source = "subCategories", target = "subCategories", qualifiedByName = "mapSubCategories")
    @Mapping(source = "products", target = "products", qualifiedByName = "mapProducts")
    CategoryResponseDTO toDTO(Category category, @Context ProductMapper productMapper);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "subCategories", ignore = true)
    @Mapping(source = "parentCategoryId", target = "parentCategory.id")
    Category toEntity(CategoryRequestDTO dto);

    @Named("mapSubCategories")
    default Set<SubCategoryDTO> mapSubCategories(Set<Category> categories) {
        if (categories == null) return null;
        return categories.stream()
                .map(c -> SubCategoryDTO.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .build())
                .collect(Collectors.toSet());
    }

    @Named("mapProducts")
    default Set<ProductResponseDTO> mapProducts(Set<Product> products, @Context ProductMapper productMapper) {
        if (products == null) return null;
        return products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toSet());
    }
}