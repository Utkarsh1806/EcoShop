//package Ecoshop.Product.Mapper;
//
//import Ecoshop.Product.DTO.*;
//import Ecoshop.Product.Entity.*;
//import org.mapstruct.*;
//
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Mapper(componentModel = "spring")
//public interface CategoryMapper {
//
//        @Mapping(source = "parentCategory.id", target = "parentCategoryId")
//        @Mapping(source = "parentCategory.name", target = "parentCategoryName")
//        CategoryResponseDTO toDTO(Category category);
//
//        @Mapping(source = "parentCategoryId", target = "parentCategory.id")
//        Category toEntity(CategoryRequestDTO dto);
//
//}
