package com.inventory.authservice.client;

import com.inventory.authservice.dto.AccessEventRequest;
import com.inventory.authservice.config.CloudRunMetadataResolver;
import com.inventory.authservice.config.CloudRunRestClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuditClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public AuditClient(RestClient.Builder builder,
                       CloudRunRestClientFactory restClientFactory,
                       CloudRunMetadataResolver metadataResolver,
                       @Value("${app.clients.reporting.base-url:}") String configuredBaseUrl,
                       @Value("${app.security.internal-api-key:inventory-internal-key}") String internalApiKey) {
        String baseUrl = metadataResolver.resolveServiceUrl(configuredBaseUrl, "reporting-service", 8084);
        this.restClient = restClientFactory.create(builder, baseUrl);
        this.internalApiKey = internalApiKey;
    }

    public void registerAccessEvent(String bearerToken, AccessEventRequest request) {
        restClient.post()
                .uri("/api/internal/access-events")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header("X-Internal-Api-Key", internalApiKey)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
