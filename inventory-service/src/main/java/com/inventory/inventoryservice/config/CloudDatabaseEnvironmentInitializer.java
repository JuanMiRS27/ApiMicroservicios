package com.inventory.inventoryservice.config;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.util.StringUtils;

public class CloudDatabaseEnvironmentInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        if (StringUtils.hasText(environment.getProperty("spring.datasource.url"))) {
            return;
        }

        Map<String, Object> properties = new LinkedHashMap<>();
        String databaseUrl = firstNonBlank(
                environment.getProperty("DATABASE_URL"),
                environment.getProperty("DATABASE_PUBLIC_URL"));

        if (StringUtils.hasText(databaseUrl)) {
            applyDatabaseUrl(databaseUrl, properties, environment);
        } else if (StringUtils.hasText(environment.getProperty("PGHOST"))) {
            String jdbcUrl = "jdbc:postgresql://%s:%s/%s".formatted(
                    environment.getProperty("PGHOST"),
                    environment.getProperty("PGPORT", "5432"),
                    environment.getProperty("PGDATABASE", "inventory_db"));
            properties.put("spring.datasource.url", jdbcUrl);
        }

        putIfMissing(properties, environment, "spring.datasource.username", "PGUSER");
        putIfMissing(properties, environment, "spring.datasource.password", "PGPASSWORD");
        putIfMissing(properties, environment, "spring.datasource.hikari.data-source-properties.sslmode", "PGSSLMODE");

        if (!properties.isEmpty()) {
            environment.getPropertySources()
                    .addFirst(new MapPropertySource("cloudDatabaseEnvironment", properties));
        }
    }

    private void applyDatabaseUrl(String databaseUrl, Map<String, Object> properties, ConfigurableEnvironment environment) {
        try {
            URI uri = new URI(databaseUrl);
            String scheme = uri.getScheme();
            if (!StringUtils.hasText(scheme) || !scheme.startsWith("postgres")) {
                return;
            }

            String jdbcUrl = "jdbc:postgresql://%s:%s%s%s".formatted(
                    uri.getHost(),
                    uri.getPort() > 0 ? uri.getPort() : 5432,
                    uri.getPath(),
                    uri.getQuery() == null ? "" : "?" + uri.getQuery());
            properties.put("spring.datasource.url", jdbcUrl);

            if (!StringUtils.hasText(environment.getProperty("spring.datasource.username"))) {
                String userInfo = uri.getUserInfo();
                if (StringUtils.hasText(userInfo)) {
                    String[] credentials = userInfo.split(":", 2);
                    if (credentials.length > 0 && StringUtils.hasText(credentials[0])) {
                        properties.put("spring.datasource.username", decode(credentials[0]));
                    }
                    if (credentials.length > 1 && StringUtils.hasText(credentials[1])) {
                        properties.put("spring.datasource.password", decode(credentials[1]));
                    }
                }
            }
        } catch (URISyntaxException ignored) {
        }
    }

    private void putIfMissing(Map<String, Object> properties,
                              ConfigurableEnvironment environment,
                              String targetProperty,
                              String sourceProperty) {
        if (!StringUtils.hasText(environment.getProperty(targetProperty))
                && !properties.containsKey(targetProperty)
                && StringUtils.hasText(environment.getProperty(sourceProperty))) {
            properties.put(targetProperty, environment.getProperty(sourceProperty));
        }
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return null;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
