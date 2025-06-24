package Ecoshop.Product.Service.Impl;

import Ecoshop.Exceptions.ResourceAlreadyExistException;
import Ecoshop.Exceptions.ResourceNotFoundException;
import Ecoshop.Product.DTO.BrandRequestDTO;
import Ecoshop.Product.DTO.BrandResponseDTO;
import Ecoshop.Product.DTO.CategoryResponseDTO;
import Ecoshop.Product.DTO.PagedResponse;
import Ecoshop.Product.Entity.Brand;
import Ecoshop.Product.Entity.Category;
import Ecoshop.Product.Entity.Product;
import Ecoshop.Product.Mapper.BrandMapper;
import Ecoshop.Product.Mapper.ProductMapper;
import Ecoshop.Product.Repository.BrandRepository;
import Ecoshop.Product.Repository.ProductRepository;
import Ecoshop.Product.Service.BrandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final ProductRepository productRepository;
    private final BrandMapper brandMapper;
    private final ProductMapper productMapper;

    public BrandServiceImpl(BrandRepository brandRepository, ProductRepository productRepository, BrandMapper brandMapper, ProductMapper productMapper) {
        this.brandRepository = brandRepository;
        this.productRepository = productRepository;
        this.brandMapper = brandMapper;
        this.productMapper = productMapper;
    }

    private static final Logger log = LoggerFactory.getLogger(BrandServiceImpl.class);

    @Override
    @Transactional
    public BrandResponseDTO create(BrandRequestDTO brandRequestDTO) throws ResourceAlreadyExistException, ResourceNotFoundException {
        log.info("Creating brand: {}", brandRequestDTO.getName());

        if (brandRepository.findByName(brandRequestDTO.getName())) {
            log.warn("Brand already exists with name: {}", brandRequestDTO.getName());
            throw new ResourceAlreadyExistException("Brand with this name already exists");
        }

        Set<Product> products = (brandRequestDTO.getProductIds() != null)
                ? brandRequestDTO.getProductIds().stream()
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

        Brand brand = brandMapper.toEntity(brandRequestDTO);
        brand.setProducts(products);

        Brand saved = brandRepository.save(brand);
        log.info("Brand created successfully with ID: {}", saved.getId());

        return brandMapper.toDTO(saved);
    }

    @Override
    @Transactional
    public BrandResponseDTO updateBrand(Long id, BrandRequestDTO brandRequestDTO) throws ResourceNotFoundException, ResourceAlreadyExistException {
        log.info("Updating brand with ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));

        if (!brand.getName().equalsIgnoreCase(brandRequestDTO.getName())
                && brandRepository.existsByName(brandRequestDTO.getName())) {
            throw new ResourceAlreadyExistException("Another brand already exists with name: " + brandRequestDTO.getName());
        }

        brand.setName(brandRequestDTO.getName());
        brand.setLogoUrl(brandRequestDTO.getLogoUrl());
        brand.setDescription(brandRequestDTO.getDescription());

        Set<Product> updatedProducts = (brandRequestDTO.getProductIds() != null)
                ? brandRequestDTO.getProductIds().stream()
                .map(productId -> {
                    try {
                        return productRepository.findById(productId)
                                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
                    } catch (ResourceNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toSet())
                : Set.of();

        brand.setProducts(updatedProducts);

        Brand updatedBrand = brandRepository.save(brand);

        log.info("Brand updated successfully: {}", updatedBrand.getId());
        return brandMapper.toDTO(updatedBrand);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<BrandResponseDTO> getAll(int page, int size) {
        log.info("Fetching all brands, page: {}, size: {}", page, size);
       Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
       Page<Brand> brandPage = brandRepository.findAll(pageable);

       List<BrandResponseDTO> content = brandPage.getContent().stream().map(brandMapper::toDTO).toList();

        return new PagedResponse<>(
                content,
                brandPage.getNumber(),
                brandPage.getSize(),
                brandPage.getTotalElements(),
                brandPage.getTotalPages(),
                brandPage.isLast(),
                brandPage.isFirst()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrand(Long id) throws ResourceNotFoundException {
        log.info("Getting brand: {}", id);
        Brand brand = brandRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Brand not exists with ID: " + id)
        );
        return brandMapper.toDTO(brand);
    }

    @Override
    @Transactional
    public BrandResponseDTO delete(Long id) throws ResourceNotFoundException {
        log.info("Attempting to delete brand with ID: {}", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand with ID '" + id + "' not found."));

        BrandResponseDTO response = brandMapper.toDTO(brand);

        brandRepository.delete(brand);
        log.info("Category with ID {} deleted successfully.", id);
        return response;
    }
}
