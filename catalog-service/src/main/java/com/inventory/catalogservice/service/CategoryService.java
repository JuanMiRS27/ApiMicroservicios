package com.inventory.catalogservice.service;

import com.inventory.catalogservice.dto.CategoryRequest;
import com.inventory.catalogservice.dto.CategoryResponse;
import com.inventory.catalogservice.entity.Category;
import com.inventory.catalogservice.exception.ResourceNotFoundException;
import com.inventory.catalogservice.repository.CategoryRepository;
import com.inventory.catalogservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .filter(Category::isActive)
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("La categoria ya existe");
        }

        Category category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        category.setActive(true);
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getRequired(id);
        validateNameAvailability(request.name(), category.getId());

        category.setName(request.name());
        category.setDescription(request.description());
        return toResponse(categoryRepository.save(category));
    }

    @Transactional
    public void deactivate(Long id) {
        Category category = getRequired(id);
        if (!category.isActive()) {
            return;
        }
        if (productRepository.existsByCategoryIdAndActiveTrue(id)) {
            throw new IllegalArgumentException("No se puede desactivar una categoria con productos activos");
        }
        category.setActive(false);
        categoryRepository.save(category);
    }

    public Category getRequired(Long id) {
        return categoryRepository.findById(id)
                .filter(Category::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    }

    private void validateNameAvailability(String name, Long currentCategoryId) {
        categoryRepository.findByNameIgnoreCase(name)
                .filter(existingCategory -> !existingCategory.getId().equals(currentCategoryId))
                .ifPresent(existingCategory -> {
                    throw new IllegalArgumentException("La categoria ya existe");
                });
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.isActive(),
                category.getCreatedAt()
        );
    }
}
