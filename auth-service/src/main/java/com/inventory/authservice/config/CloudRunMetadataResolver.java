package com.inventory.authservice.config;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CloudRunMetadataResolver {

    private static final String METADATA_URL = "http://metadata.google.internal/computeMetadata/v1";

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final String cloudRunServiceName;
    private volatile String region;
    private volatile String projectNumber;

    public CloudRunMetadataResolver(@Value("${K_SERVICE:}") String cloudRunServiceName) {
        this.cloudRunServiceName = cloudRunServiceName;
    }

    public boolean isCloudRun() {
        return StringUtils.hasText(cloudRunServiceName);
    }

    public String resolveServiceUrl(String configuredBaseUrl, String serviceName, int localPort) {
        if (StringUtils.hasText(configuredBaseUrl)) {
            return trimTrailingSlash(configuredBaseUrl);
        }
        if (!isCloudRun()) {
            return "http://localhost:%d".formatted(localPort);
        }
        return "https://%s-%s.%s.run.app".formatted(serviceName, getProjectNumber(), getRegion());
    }

    private String getProjectNumber() {
        if (projectNumber == null) {
            projectNumber = fetchMetadata("/project/numeric-project-id");
        }
        return projectNumber;
    }

    private String getRegion() {
        if (region == null) {
            String zonePath = fetchMetadata("/instance/region");
            region = zonePath.substring(zonePath.lastIndexOf('/') + 1);
        }
        return region;
    }

    private String fetchMetadata(String path) {
        HttpRequest request = HttpRequest.newBuilder(URI.create(METADATA_URL + path))
                .header("Metadata-Flavor", "Google")
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300 && StringUtils.hasText(response.body())) {
                return response.body().trim();
            }
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
        throw new IllegalStateException("No se pudo resolver metadata de Cloud Run.");
    }

    private String trimTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
