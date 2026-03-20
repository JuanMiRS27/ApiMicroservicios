package com.inventory.catalogservice.repository;

import com.inventory.catalogservice.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySkuIgnoreCase(String sku);

    Optional<Product> findBySkuIgnoreCase(String sku);

    List<Product> findByCategoryId(Long categoryId);

    boolean existsByCategoryIdAndActiveTrue(Long categoryId);
}
