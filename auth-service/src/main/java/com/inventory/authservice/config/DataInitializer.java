package com.inventory.authservice.config;

import com.inventory.authservice.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner loadUsers(UserService userService) {
        return args -> userService.seedDefaultUsers();
    }
}
