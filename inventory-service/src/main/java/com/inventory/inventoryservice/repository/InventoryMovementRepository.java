package com.inventory.inventoryservice.repository;

import com.inventory.inventoryservice.entity.InventoryMovement;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {

    List<InventoryMovement> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);
}
