package com.inventory.inventoryservice.service;

import com.inventory.inventoryservice.client.CatalogClient;
import com.inventory.inventoryservice.dto.MovementRequest;
import com.inventory.inventoryservice.dto.MovementResponse;
import com.inventory.inventoryservice.dto.MovementSummaryItem;
import com.inventory.inventoryservice.dto.StockResponse;
import com.inventory.inventoryservice.dto.StockSummaryItem;
import com.inventory.inventoryservice.entity.InventoryMovement;
import com.inventory.inventoryservice.entity.MovementType;
import com.inventory.inventoryservice.entity.ProductStock;
import com.inventory.inventoryservice.repository.InventoryMovementRepository;
import com.inventory.inventoryservice.repository.ProductStockRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final ProductStockRepository productStockRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final CatalogClient catalogClient;

    public InventoryService(ProductStockRepository productStockRepository,
                            InventoryMovementRepository inventoryMovementRepository,
                            CatalogClient catalogClient) {
        this.productStockRepository = productStockRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.catalogClient = catalogClient;
    }

    @Transactional
    public MovementResponse registerEntry(MovementRequest request, String username, String bearerToken) {
        catalogClient.validateProduct(request.productId(), bearerToken);

        ProductStock stock = getOrCreateStock(request.productId());
        stock.setQuantity(stock.getQuantity() + request.quantity());
        stock.setUpdatedAt(LocalDateTime.now());
        productStockRepository.save(stock);

        InventoryMovement movement = buildMovement(request, username, MovementType.ENTRY);
        return toResponse(inventoryMovementRepository.save(movement));
    }

    @Transactional
    public MovementResponse registerExit(MovementRequest request, String username, String bearerToken) {
        catalogClient.validateProduct(request.productId(), bearerToken);

        ProductStock stock = getOrCreateStock(request.productId());
        if (stock.getQuantity() < request.quantity()) {
            throw new IllegalArgumentException("Stock insuficiente para la salida");
        }

        stock.setQuantity(stock.getQuantity() - request.quantity());
        stock.setUpdatedAt(LocalDateTime.now());
        productStockRepository.save(stock);

        InventoryMovement movement = buildMovement(request, username, MovementType.EXIT);
        return toResponse(inventoryMovementRepository.save(movement));
    }

    public StockResponse getStock(Long productId) {
        ProductStock stock = productStockRepository.findByProductId(productId)
                .orElseGet(() -> {
                    ProductStock newStock = new ProductStock();
                    newStock.setProductId(productId);
                    newStock.setQuantity(0);
                    newStock.setUpdatedAt(LocalDateTime.now());
                    return newStock;
                });
        return new StockResponse(stock.getProductId(), stock.getQuantity(), stock.getUpdatedAt());
    }

    public List<MovementResponse> findAllMovements() {
        return inventoryMovementRepository.findAll().stream()
                .sorted(Comparator.comparing(InventoryMovement::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    public List<StockSummaryItem> stockSummary() {
        return productStockRepository.findAll().stream()
                .map(stock -> new StockSummaryItem(stock.getProductId(), stock.getQuantity()))
                .toList();
    }

    public List<MovementSummaryItem> movementSummary(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(LocalTime.MAX);
        return inventoryMovementRepository.findByCreatedAtBetween(start, end).stream()
                .map(movement -> new MovementSummaryItem(
                        movement.getId(),
                        movement.getProductId(),
                        movement.getMovementType(),
                        movement.getQuantity(),
                        movement.getDestination(),
                        movement.getCreatedBy(),
                        movement.getCreatedAt()
                ))
                .toList();
    }

    private ProductStock getOrCreateStock(Long productId) {
        return productStockRepository.findByProductId(productId)
                .orElseGet(() -> {
                    ProductStock stock = new ProductStock();
                    stock.setProductId(productId);
                    stock.setQuantity(0);
                    stock.setUpdatedAt(LocalDateTime.now());
                    return stock;
                });
    }

    private InventoryMovement buildMovement(MovementRequest request, String username, MovementType movementType) {
        InventoryMovement movement = new InventoryMovement();
        movement.setProductId(request.productId());
        movement.setMovementType(movementType);
        movement.setQuantity(request.quantity());
        movement.setReference(request.reference());
        movement.setDestination(request.destination());
        movement.setNotes(request.notes());
        movement.setCreatedBy(username);
        return movement;
    }

    private MovementResponse toResponse(InventoryMovement movement) {
        return new MovementResponse(
                movement.getId(),
                movement.getProductId(),
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getReference(),
                movement.getDestination(),
                movement.getNotes(),
                movement.getCreatedBy(),
                movement.getCreatedAt()
        );
    }
}
