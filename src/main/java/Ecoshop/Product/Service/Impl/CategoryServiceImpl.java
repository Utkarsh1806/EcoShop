package Ecoshop.Product.Service.Impl;

import Ecoshop.Exceptions.ProductDoesNotExistsException;
import Ecoshop.Exceptions.ResourceAlreadyExistException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.CategoryRequestDTO;
import Ecoshop.Product.DTO.CategoryResponseDTO;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Category;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Mapper.CategoryMapper;
import Ecoshop.Product.Mapper.ProductMapper;
import Ecoshop.Product.Repository.CategoryRepository;
import Ecoshop.Product.Repository.ProductRepository;
import Ecoshop.Product.Service.CategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    private final CategoryMapper categoryMapper;
    private final ProductMapper productMapper;

    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, CategoryMapper categoryMapper, ProductMapper productMapper) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryMapper = categoryMapper;
        this.productMapper = productMapper;
    }

    private static final Logger log = LoggerFactory.getLogger(CategoryServiceImpl.class);

    @Override
    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO categoryRequestDTO) throws ResourceAlreadyExistException, ResourceNotFoundException {
        log.info("Creating category: {}", categoryRequestDTO.getName());

        boolean exists = categoryRepository.existsByName(categoryRequestDTO.getName());
        if (exists) {
            log.warn("Category already exists with name: {} under parent ID: {}", categoryRequestDTO.getName(), categoryRequestDTO.getParentCategoryId());
            throw new ResourceAlreadyExistException("Category with this name already exists");
        }

        Set<Category> subcategories = (categoryRequestDTO.getSubCategoriesId() != null)
                ? categoryRequestDTO.getSubCategoriesId().stream()
                .map(id -> {
                    try {
                        return categoryRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Sub-Category not found with ID: " + id));
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet())
                : Set.of();

        Set<Product> products = (categoryRequestDTO.getProductCategoryIds() != null)
                ? categoryRequestDTO.getProductCategoryIds().stream()
                .map(id -> {
                    try {
                        return productRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet())
                : Set.of();

        Category category = categoryMapper.toEntity(categoryRequestDTO);
        category.setSubCategories(subcategories);
        category.setProducts(products);


        if (categoryRequestDTO.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(categoryRequestDTO.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + categoryRequestDTO.getParentCategoryId()));
            category.setParentCategory(parent);
        } else {
            category.setParentCategory(null);
        }

        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", saved.getId());

        return categoryMapper.toDTO(saved, productMapper);
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO categoryRequestDTO) throws ResourceNotFoundException {
        log.info("Updating category with ID: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        existingCategory.setName(categoryRequestDTO.getName());
        existingCategory.setIconUrl(categoryRequestDTO.getIconUrl());
        existingCategory.setDescription(categoryRequestDTO.getDescription());

        if (categoryRequestDTO.getParentCategoryId() != null) {
            Category parent = categoryRepository.findById(categoryRequestDTO.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + categoryRequestDTO.getParentCategoryId()));
            existingCategory.setParentCategory(parent);
        } else {
            existingCategory.setParentCategory(null);
        }

        if (categoryRequestDTO.getSubCategoriesId() != null && !categoryRequestDTO.getSubCategoriesId().isEmpty()) {
            Set<Category> subCategories = categoryRequestDTO.getSubCategoriesId().stream()
                    .map(subId -> {
                        try {
                            return categoryRepository.findById(subId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Sub-category not found with ID: " + subId));
                        } catch (ResourceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());
            existingCategory.setSubCategories(subCategories);
        }

        if (categoryRequestDTO.getProductCategoryIds() != null && !categoryRequestDTO.getProductCategoryIds().isEmpty()) {
            Set<Product> products = categoryRequestDTO.getProductCategoryIds().stream()
                    .map(prodId -> {
                        try {
                            return productRepository.findById(prodId)
                                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + prodId));
                        } catch (ResourceNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .collect(Collectors.toSet());
            existingCategory.setProducts(products);
        }

        Category updated = categoryRepository.save(existingCategory);

        log.info("Category updated successfully with ID: {}", updated.getId());

        return categoryMapper.toDTO(updated, productMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CategoryResponseDTO> getAll(int page, int size) {
        log.info("Fetching all categories - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<CategoryResponseDTO> content = categoryPage.getContent().stream()
                .map(category -> categoryMapper.toDTO(category, productMapper))
                .toList();

        return new PagedResponse<>(
                content,
                categoryPage.getNumber(),
                categoryPage.getSize(),
                categoryPage.getTotalElements(),
                categoryPage.getTotalPages(),
                categoryPage.isLast(),
                categoryPage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategory(Long id) throws ResourceNotFoundException {
        log.info("Fetching category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Category not found with ID: {}", id);
                    return new ResourceNotFoundException("Category not found with ID: " + id);
                });

        CategoryResponseDTO response = categoryMapper.toDTO(category, productMapper);

        log.info("Category fetched successfully for ID: {}", id);
        return response;
    }

    @Override
    @Transactional
    public CategoryResponseDTO delete(Long id) throws ResourceNotFoundException {
        log.info("Attempting to delete category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category with ID '" + id + "' not found."));

        CategoryResponseDTO response = categoryMapper.toDTO(category, productMapper);

        categoryRepository.delete(category);
        log.info("Category with ID {} deleted successfully.", id);
        return response;
    }
}
