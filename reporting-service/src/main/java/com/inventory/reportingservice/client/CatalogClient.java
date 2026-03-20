package com.inventory.reportingservice.client;

import com.inventory.reportingservice.config.InternalApiKeyFilter;
import com.inventory.reportingservice.dto.CategoryResponse;
import com.inventory.reportingservice.dto.ProductResponse;
import java.util.Arrays;
import java.util.List;
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

    public List<ProductResponse> getProducts(String bearerToken) {
        ProductResponse[] products = restClient.get()
                .uri("/api/products")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .retrieve()
                .body(ProductResponse[].class);
        return Arrays.asList(products == null ? new ProductResponse[0] : products);
    }

    public List<CategoryResponse> getCategories(String bearerToken) {
        CategoryResponse[] categories = restClient.get()
                .uri("/api/internal/categories")
                .header(HttpHeaders.AUTHORIZATION, bearerToken)
                .header(InternalApiKeyFilter.INTERNAL_API_KEY_HEADER, internalApiKey)
                .retrieve()
                .body(CategoryResponse[].class);
        return Arrays.asList(categories == null ? new CategoryResponse[0] : categories);
    }
}
