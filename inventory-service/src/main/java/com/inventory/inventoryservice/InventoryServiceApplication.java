package com.inventory.inventoryservice;

import com.inventory.inventoryservice.config.CloudDatabaseEnvironmentInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(InventoryServiceApplication.class);
        application.addInitializers(new CloudDatabaseEnvironmentInitializer());
        application.run(args);
    }
}
