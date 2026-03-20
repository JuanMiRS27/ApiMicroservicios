package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.ProductStock;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockRepository extends JpaRepository<ProductStock, Long> {

    Optional<ProductStock> findByProductId(Long productId);
}
