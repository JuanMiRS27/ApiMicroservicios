package com.inventory.inventoryservice.client;

import com.inventory.inventoryservice.config.InternalApiKeyFilter;
import com.inventory.inventoryservice.dto.ProductValidationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public CatalogClient(RestClient.Builder builder,
                         @Value("${app.clients.catalog.base-url}") String baseUrl,
                         @Value("${app.security.internal-api-key:inventory-internal-key}") String internalApiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public ProductValidationResponse validateProduct(Long productId, String bearerToken) {
        return restClient.get()
                .uri("/api/internal/products/{id}/validation", productId)
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(InternalApiKeyFilter.INTERNAL_API_KEY_HEADER, internalApiKey)
                .retrieve()
                .body(ProductValidationResponse.class);
    }
}
