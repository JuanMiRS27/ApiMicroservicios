package com.inventory.reportingservice.config;

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
        } else if (StringUtils.hasText(environment.getProperty("INSTANCE_CONNECTION_NAME"))
                || StringUtils.hasText(environment.getProperty("CLOUD_SQL_INSTANCE_CONNECTION_NAME"))) {
            properties.put("spring.datasource.url", buildCloudSqlJdbcUrl(
                    firstNonBlank(
                            environment.getProperty("INSTANCE_CONNECTION_NAME"),
                            environment.getProperty("CLOUD_SQL_INSTANCE_CONNECTION_NAME")),
                    firstNonBlank(
                            environment.getProperty("DB_NAME"),
                            environment.getProperty("PGDATABASE"),
                            "reporting_db")));
        } else if (StringUtils.hasText(environment.getProperty("PGHOST"))) {
            properties.put("spring.datasource.url", buildJdbcUrl(
                    environment.getProperty("PGHOST"),
                    environment.getProperty("PGPORT", "5432"),
                    environment.getProperty("PGDATABASE", "reporting_db")));
        } else if (StringUtils.hasText(environment.getProperty("DB_HOST"))) {
            properties.put("spring.datasource.url", buildJdbcUrl(
                    environment.getProperty("DB_HOST"),
                    environment.getProperty("DB_PORT", "5432"),
                    environment.getProperty("DB_NAME", "reporting_db")));
        }

        putIfMissing(properties, environment, "spring.datasource.username", "PGUSER");
        putIfMissing(properties, environment, "spring.datasource.password", "PGPASSWORD");
        putIfMissing(properties, environment, "spring.datasource.username", "DB_USER");
        putIfMissing(properties, environment, "spring.datasource.password", "DB_PASSWORD");
        putIfMissing(properties, environment, "spring.datasource.hikari.data-source-properties.sslmode", "PGSSLMODE");
        putIfMissing(properties, environment, "spring.datasource.hikari.data-source-properties.sslmode", "DB_SSL_MODE");

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

            properties.put("spring.datasource.url", buildJdbcUrl(
                    uri.getHost(),
                    String.valueOf(uri.getPort() > 0 ? uri.getPort() : 5432),
                    uri.getPath().replaceFirst("^/", ""),
                    uri.getQuery()));

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

    private String buildJdbcUrl(String host, String port, String database) {
        return buildJdbcUrl(host, port, database, null);
    }

    private String buildJdbcUrl(String host, String port, String database, String query) {
        return "jdbc:postgresql://%s:%s/%s%s".formatted(
                host,
                port,
                database,
                StringUtils.hasText(query) ? "?" + query : "");
    }

    private String buildCloudSqlJdbcUrl(String instanceConnectionName, String database) {
        return "jdbc:postgresql:///%s?cloudSqlInstance=%s&socketFactory=com.google.cloud.sql.postgres.SocketFactory"
                .formatted(database, instanceConnectionName);
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
