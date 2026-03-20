package com.inventory.catalogservice;

import com.inventory.catalogservice.config.CloudDatabaseEnvironmentInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatalogServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(CatalogServiceApplication.class);
        application.addInitializers(new CloudDatabaseEnvironmentInitializer());
        application.run(args);
    }
}
