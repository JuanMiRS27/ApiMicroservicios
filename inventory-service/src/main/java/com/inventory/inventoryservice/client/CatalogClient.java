package com.inventory.inventoryservice.client;

import com.inventory.inventoryservice.config.CloudRunMetadataResolver;
import com.inventory.inventoryservice.config.CloudRunRestClientFactory;
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
                         CloudRunRestClientFactory restClientFactory,
                         CloudRunMetadataResolver metadataResolver,
                         @Value("${app.clients.catalog.base-url:}") String configuredBaseUrl,
                         @Value("${app.security.internal-api-key:inventory-internal-key}") String internalApiKey) {
        String baseUrl = metadataResolver.resolveServiceUrl(configuredBaseUrl, "catalog-service", 8082);
        this.restClient = restClientFactory.create(builder, baseUrl);
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
