package com.inventory.reportingservice;

import com.inventory.reportingservice.config.CloudDatabaseEnvironmentInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ReportingServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ReportingServiceApplication.class);
        application.addInitializers(new CloudDatabaseEnvironmentInitializer());
        application.run(args);
    }
}
