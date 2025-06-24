package Ecoshop.Product.Service.Impl;

import Ecoshop.Exceptions.ProductAlreadyExistsException;
import Ecoshop.Exceptions.ProductDoesNotExistsException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.DTO.ProductRequestDTO;
import Ecoshop.Product.DTO.ProductResponseDTO;
import Ecoshop.Product.Entity.Brand;
import Ecoshop.Product.Entity.Category;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Mapper.ProductMapper;
import Ecoshop.Product.Repository.BrandRepository;
import Ecoshop.Product.Repository.CategoryRepository;
import Ecoshop.Product.Repository.ProductRepository;
import Ecoshop.Product.Service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProductsServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    private static final Logger log = LoggerFactory.getLogger(ProductsServiceImpl.class);

    public ProductsServiceImpl(ProductRepository productRepository,
                               ProductMapper productMapper,
                               BrandRepository brandRepository,
                               CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    @Transactional
    public ProductResponseDTO addProduct(ProductRequestDTO productDTO) throws ResourceNotFoundException {
        log.info("Creating product: {}", productDTO.getName());
        log.debug("Product details: {}", productDTO);

        boolean exists = productRepository.existsByNameAndBrandIdAndCategoryId(
                productDTO.getName(),
                productDTO.getBrandId(),
                productDTO.getCategoryId()
        );
        if (exists) {
            throw new ProductAlreadyExistsException("Product with name '" + productDTO.getName() +
                    "' already exists under the specified brand and category.");
        }

        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + productDTO.getCategoryId()));

        Brand brand = brandRepository.findById(productDTO.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + productDTO.getBrandId()));

        Product product = productMapper.toEntity(productDTO);
        product.setCategory(category);
        product.setBrand(brand);

        Product savedProduct = productRepository.save(product);

        ProductResponseDTO responseDTO = productMapper.toDTO(savedProduct);
        responseDTO.setCategoryName(category.getName());
        responseDTO.setBrandName(brand.getName());

        log.info("Product created successfully with ID: {}", savedProduct.getId());
        return responseDTO;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDTO> getProducts(int size, int page) {
        log.info("Fetching products - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productRepository.findAll(pageable);
        List<ProductResponseDTO> content = productPage.getContent()
                .stream().map(productMapper::toDTO).toList();

        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                productPage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDTO> getProductsByBrand(String brandName, int page, int size) {
        log.info("Fetching products by category - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productRepository.findByBrand_NameIgnoreCase(brandName, pageable);

        List<ProductResponseDTO> content = productPage.getContent()
                .stream().map(productMapper::toDTO).toList();
        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                productPage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDTO> getProductsByIsReturnable(Boolean isReturnable,int size, int page) {
        log.info("Fetching products by isReturnable: {}", isReturnable);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage = productRepository.findByIsReturnable(pageable, isReturnable);

        List<ProductResponseDTO> content = productPage.getContent()
                .stream().map(productMapper ::toDTO).toList();
        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                productPage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDTO> getProductsByIsVegetarian(Boolean isVegetarian,int size, int page) {
        log.info("Fetching products by isVegeterian: {}", isVegetarian);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Product> productPage = productRepository.findByIsVegetarian(pageable, isVegetarian);

        List<ProductResponseDTO> content = productPage.getContent()
                .stream().map(productMapper ::toDTO).toList();
        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                productPage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ProductResponseDTO> getProductsByCategory(String category, int page, int size) {
        log.info("Fetching products by category - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage = productRepository.findByCategory_NameIgnoreCase(category, pageable);

        List<ProductResponseDTO> content = productPage.getContent()
                .stream().map(productMapper::toDTO).toList();
        return new PagedResponse<>(
                content,
                productPage.getNumber(),
                productPage.getSize(),
                productPage.getTotalElements(),
                productPage.getTotalPages(),
                productPage.isLast(),
                productPage.isFirst()
        );

    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponseDTO getProduct(Long id) throws ProductDoesNotExistsException {
        log.info("Fetching product by ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductDoesNotExistsException(
                        "Product with ID '" + id + "' does not exist."));

        return productMapper.toDTO(product);
    }

    @Override
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO dto) throws ProductDoesNotExistsException, ResourceNotFoundException {
        log.info("Updating product with ID: {}", id);
        log.debug("Update details: {}", dto);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ProductDoesNotExistsException("Product with ID '" + id + "' not found."));

        boolean duplicateExists = productRepository.existsByNameAndBrandIdAndCategoryIdAndIdNot(
                dto.getName(), dto.getBrandId(), dto.getCategoryId(), id);

        if (duplicateExists) {
            throw new ProductAlreadyExistsException("Another product with same name, brand and category already exists.");
        }

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        Brand brand = brandRepository.findById(dto.getBrandId())
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + dto.getBrandId()));

        existingProduct.setName(dto.getName());
        existingProduct.setDescription(dto.getDescription());
        existingProduct.setPrice(dto.getPrice());
        existingProduct.setDiscountPrice(dto.getDiscountPrice());
        existingProduct.setUnit(dto.getUnit());
        existingProduct.setStockQuantity(dto.getStockQuantity());
        existingProduct.setInStock(dto.getInStock());
        existingProduct.setImageUrl(dto.getImageUrl());
        existingProduct.setIsReturnable(dto.getIsReturnable());
        existingProduct.setIsVegetarian(dto.getIsVegetarian());
        existingProduct.setCategory(category);
        existingProduct.setBrand(brand);

        Product updatedProduct = productRepository.save(existingProduct);

        ProductResponseDTO response = productMapper.toDTO(updatedProduct);
        response.setCategoryName(category.getName());
        response.setBrandName(brand.getName());

        log.info("Product updated successfully for ID: {}", id);
        return response;
    }

    @Override
    @Transactional
    public ProductResponseDTO deleteProduct(Long id) throws ProductDoesNotExistsException {
        log.info("Attempting to delete product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductDoesNotExistsException("Product with ID '" + id + "' not found."));

        ProductResponseDTO response = productMapper.toDTO(product);

        productRepository.delete(product);
        log.info("Product with ID {} deleted successfully.", id);
        return response;
    }
}
