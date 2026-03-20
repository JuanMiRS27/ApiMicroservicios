package com.inventory.authservice.config;

import java.net.URI;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class CloudRunRestClientFactory {

    private final CloudRunMetadataResolver metadataResolver;
    private final CloudRunIdTokenProvider idTokenProvider;

    public CloudRunRestClientFactory(CloudRunMetadataResolver metadataResolver,
                                     CloudRunIdTokenProvider idTokenProvider) {
        this.metadataResolver = metadataResolver;
        this.idTokenProvider = idTokenProvider;
    }

    public RestClient create(RestClient.Builder builder, String baseUrl) {
        RestClient.Builder configuredBuilder = builder.clone().baseUrl(baseUrl);
        String audience = resolveAudience(baseUrl);
        if (!StringUtils.hasText(audience)) {
            return configuredBuilder.build();
        }

        return configuredBuilder.requestInterceptor((request, body, execution) -> {
            request.getHeaders().set("X-Serverless-Authorization", "Bearer " + idTokenProvider.getToken(audience));
            return execution.execute(request, body);
        }).build();
    }

    private String resolveAudience(String baseUrl) {
        if (!metadataResolver.isCloudRun()) {
            return null;
        }
        URI uri = URI.create(baseUrl);
        if (!"https".equalsIgnoreCase(uri.getScheme()) || !StringUtils.hasText(uri.getHost())
                || !uri.getHost().endsWith(".run.app")) {
            return null;
        }
        return "%s://%s/".formatted(uri.getScheme(), uri.getAuthority());
    }
}
