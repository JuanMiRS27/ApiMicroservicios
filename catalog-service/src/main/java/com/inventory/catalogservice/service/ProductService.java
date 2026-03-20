package com.inventory.catalogservice.service;

import com.inventory.catalogservice.dto.ProductRequest;
import com.inventory.catalogservice.dto.ProductResponse;
import com.inventory.catalogservice.dto.ProductValidationResponse;
import com.inventory.catalogservice.entity.Product;
import com.inventory.catalogservice.exception.ResourceNotFoundException;
import com.inventory.catalogservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .filter(Product::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (productRepository.existsBySkuIgnoreCase(request.sku())) {
            throw new IllegalArgumentException("El SKU ya existe");
        }

        categoryService.getRequired(request.categoryId());

        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategoryId(request.categoryId());
        product.setUnitPrice(request.unitPrice());
        product.setActive(true);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = getRequired(id);
        validateSkuAvailability(request.sku(), product.getId());
        categoryService.getRequired(request.categoryId());

        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setCategoryId(request.categoryId());
        product.setUnitPrice(request.unitPrice());

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivate(Long id) {
        Product product = getRequired(id);
        if (!product.isActive()) {
            return;
        }
        product.setActive(false);
        productRepository.save(product);
    }

    public ProductValidationResponse validateProduct(Long id) {
        Product product = getRequired(id);
        return new ProductValidationResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getCategoryId(),
                product.isActive()
        );
    }

    private Product getRequired(Long id) {
        return productRepository.findById(id)
                .filter(Product::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado"));
    }

    private void validateSkuAvailability(String sku, Long currentProductId) {
        productRepository.findBySkuIgnoreCase(sku)
                .filter(existingProduct -> !existingProduct.getId().equals(currentProductId))
                .ifPresent(existingProduct -> {
                    throw new IllegalArgumentException("El SKU ya existe");
                });
    }

    private ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getCategoryId(),
                product.getUnitPrice(),
                product.isActive(),
                product.getCreatedAt()
        );
    }
}
