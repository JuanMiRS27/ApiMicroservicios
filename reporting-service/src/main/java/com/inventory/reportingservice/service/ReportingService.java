package com.inventory.reportingservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.reportingservice.client.CatalogClient;
import com.inventory.reportingservice.client.InventoryClient;
import com.inventory.reportingservice.dto.AccessEventRequest;
import com.inventory.reportingservice.dto.CategoryResponse;
import com.inventory.reportingservice.dto.MovementReportItem;
import com.inventory.reportingservice.dto.MovementReportResponse;
import com.inventory.reportingservice.dto.MovementSummaryItem;
import com.inventory.reportingservice.dto.ProductResponse;
import com.inventory.reportingservice.dto.ReportHistoryItem;
import com.inventory.reportingservice.dto.StockReportItem;
import com.inventory.reportingservice.dto.StockReportResponse;
import com.inventory.reportingservice.dto.StockSummaryItem;
import com.inventory.reportingservice.dto.UserAccessLogResponse;
import com.inventory.reportingservice.entity.AccessEventType;
import com.inventory.reportingservice.entity.GeneratedReport;
import com.inventory.reportingservice.entity.UserAccessLog;
import com.inventory.reportingservice.repository.GeneratedReportRepository;
import com.inventory.reportingservice.repository.UserAccessLogRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportingService {

    private final CatalogClient catalogClient;
    private final InventoryClient inventoryClient;
    private final GeneratedReportRepository generatedReportRepository;
    private final UserAccessLogRepository userAccessLogRepository;
    private final ObjectMapper objectMapper;

    public ReportingService(CatalogClient catalogClient,
                            InventoryClient inventoryClient,
                            GeneratedReportRepository generatedReportRepository,
                            UserAccessLogRepository userAccessLogRepository,
                            ObjectMapper objectMapper) {
        this.catalogClient = catalogClient;
        this.inventoryClient = inventoryClient;
        this.generatedReportRepository = generatedReportRepository;
        this.userAccessLogRepository = userAccessLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StockReportResponse generateStockReport(String requestedBy, String bearerToken) {
        List<ProductResponse> products = catalogClient.getProducts(bearerToken);
        List<CategoryResponse> categories = catalogClient.getCategories(bearerToken);
        List<StockSummaryItem> stockItems = inventoryClient.getStockSummary(bearerToken);

        Map<Long, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::id, Function.identity()));
        Map<Long, String> categoryMap = categories.stream()
                .collect(Collectors.toMap(CategoryResponse::id, CategoryResponse::name));
        Map<Long, Integer> stockMap = stockItems.stream()
                .collect(Collectors.toMap(StockSummaryItem::productId, StockSummaryItem::quantity));

        List<StockReportItem> items = products.stream()
                .map(product -> new StockReportItem(
                        product.id(),
                        product.name(),
                        product.sku(),
                        categoryMap.getOrDefault(product.categoryId(), "Sin categoria"),
                        stockMap.getOrDefault(product.id(), 0)
                ))
                .filter(item -> item.productId() != null)
                .toList();

        StockReportResponse response = new StockReportResponse(LocalDateTime.now(), items);
        saveReport("STOCK_SUMMARY", requestedBy, Map.of("scope", "all"), response);
        return response;
    }

    @Transactional
    public MovementReportResponse generateMovementReport(LocalDate from,
                                                         LocalDate to,
                                                         String requestedBy,
                                                         String bearerToken) {
        List<ProductResponse> products = catalogClient.getProducts(bearerToken);
        List<MovementSummaryItem> movements = inventoryClient.getMovementSummary(bearerToken, from, to);

        Map<Long, ProductResponse> productMap = products.stream()
                .collect(Collectors.toMap(ProductResponse::id, Function.identity()));

        List<MovementReportItem> items = movements.stream()
                .map(movement -> {
                    ProductResponse product = productMap.get(movement.productId());
                    String productName = product != null ? product.name() : "Desconocido";
                    String sku = product != null ? product.sku() : "N/A";
                    return new MovementReportItem(
                            movement.id(),
                            movement.productId(),
                            productName,
                            sku,
                            movement.movementType(),
                            movement.quantity(),
                            movement.destination(),
                            movement.createdBy(),
                            movement.createdAt()
                    );
                })
                .toList();

        int totalEntries = items.stream()
                .filter(item -> "ENTRY".equals(item.movementType()))
                .mapToInt(MovementReportItem::quantity)
                .sum();
        int totalExits = items.stream()
                .filter(item -> "EXIT".equals(item.movementType()))
                .mapToInt(MovementReportItem::quantity)
                .sum();

        MovementReportResponse response = new MovementReportResponse(
                from,
                to,
                LocalDateTime.now(),
                totalEntries,
                totalExits,
                items
        );
        saveReport("MOVEMENT_SUMMARY", requestedBy, Map.of("from", from, "to", to), response);
        return response;
    }

    public List<ReportHistoryItem> history() {
        return generatedReportRepository.findAll().stream()
                .map(report -> new ReportHistoryItem(
                        report.getId(),
                        report.getReportType(),
                        report.getRequestedBy(),
                        report.getParametersJson(),
                        report.getCreatedAt()
                ))
                .toList();
    }

    @Transactional
    public UserAccessLogResponse registerAccessEvent(AccessEventRequest request) {
        UserAccessLog log = new UserAccessLog();
        log.setUsername(request.username());
        log.setRole(request.role());
        log.setEventType(AccessEventType.valueOf(request.eventType()));
        return toAccessResponse(userAccessLogRepository.save(log));
    }

    public List<UserAccessLogResponse> accessHistory() {
        return userAccessLogRepository.findAll().stream()
                .map(this::toAccessResponse)
                .toList();
    }

    private void saveReport(String type, String requestedBy, Object parameters, Object payload) {
        GeneratedReport report = new GeneratedReport();
        report.setReportType(type);
        report.setRequestedBy(requestedBy);
        report.setParametersJson(toJson(parameters));
        report.setPayloadJson(toJson(payload));
        generatedReportRepository.save(report);
    }

    private String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar el reporte", exception);
        }
    }

    private UserAccessLogResponse toAccessResponse(UserAccessLog log) {
        return new UserAccessLogResponse(
                log.getId(),
                log.getUsername(),
                log.getRole(),
                log.getEventType().name(),
                log.getCreatedAt()
        );
    }
}
