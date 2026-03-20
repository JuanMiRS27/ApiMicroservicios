package com.inventory.authservice.dto;

import com.inventory.authservice.entity.Role;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        Role role,
        boolean active,
        LocalDateTime createdAt
) {
}
