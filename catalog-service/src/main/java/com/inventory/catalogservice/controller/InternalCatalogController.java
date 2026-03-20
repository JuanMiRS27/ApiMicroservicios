package com.inventory.catalogservice.controller;

import com.inventory.catalogservice.dto.CategoryResponse;
import com.inventory.catalogservice.service.CategoryService;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
public class InternalCatalogController {

    private final CategoryService categoryService;

    public InternalCatalogController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryResponse>> findAllCategories() {
        return ResponseEntity.ok(categoryService.findAll());
    }
}
