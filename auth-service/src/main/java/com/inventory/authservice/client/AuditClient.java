package com.inventory.authservice.client;

import com.inventory.authservice.dto.AccessEventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuditClient {

    private final RestClient restClient;
    private final String internalApiKey;

    public AuditClient(RestClient.Builder builder,
                       @Value("${app.clients.reporting.base-url:http://localhost:8084}") String baseUrl,
                       @Value("${app.security.internal-api-key:inventory-internal-key}") String internalApiKey) {
        this.restClient = builder.baseUrl(baseUrl).build();
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
