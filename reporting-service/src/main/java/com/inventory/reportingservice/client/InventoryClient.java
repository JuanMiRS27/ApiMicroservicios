package com.inventory.reportingservice.client;

import com.inventory.reportingservice.config.CloudRunMetadataResolver;
import com.inventory.reportingservice.config.CloudRunRestClientFactory;
import com.inventory.reportingservice.config.InternalApiKeyFilter;
import com.inventory.reportingservice.dto.MovementSummaryItem;
import com.inventory.reportingservice.dto.StockSummaryItem;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public InventoryClient(RestClient.Builder builder,
                           CloudRunRestClientFactory restClientFactory,
                           CloudRunMetadataResolver metadataResolver,
                           @Value("${app.clients.inventory.base-url:}") String configuredBaseUrl,
                           @Value("${app.security.internal-api-key:inventory-internal-key}") String internalApiKey) {
        String baseUrl = metadataResolver.resolveServiceUrl(configuredBaseUrl, "inventory-service", 8083);
        this.restClient = restClientFactory.create(builder, baseUrl);
        this.internalApiKey = internalApiKey;
    }

    public List<StockSummaryItem> getStockSummary(String bearerToken) {
        StockSummaryItem[] items = restClient.get()
                .uri("/api/internal/stocks-summary")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(InternalApiKeyFilter.INTERNAL_API_KEY_HEADER, internalApiKey)
                .retrieve()
                .body(StockSummaryItem[].class);
        return Arrays.asList(items == null ? new StockSummaryItem[0] : items);
    }

    public List<MovementSummaryItem> getMovementSummary(String bearerToken, LocalDate from, LocalDate to) {
        MovementSummaryItem[] items = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/internal/movements-summary")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(InternalApiKeyFilter.INTERNAL_API_KEY_HEADER, internalApiKey)
                .retrieve()
                .body(MovementSummaryItem[].class);
        return Arrays.asList(items == null ? new MovementSummaryItem[0] : items);
    }
}
