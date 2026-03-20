package com.inventory.authservice;

import com.inventory.authservice.config.CloudDatabaseEnvironmentInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AuthServiceApplication.class);
        application.addInitializers(new CloudDatabaseEnvironmentInitializer());
        application.run(args);
    }
}
