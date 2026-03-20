package com.inventory.authservice.dto;

import com.inventory.authservice.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank String username,
        @NotBlank String fullName,
        @Size(min = 8) String password,
        @NotNull Role role
) {
}
