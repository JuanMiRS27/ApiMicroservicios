package com.inventory.authservice.dto;

public record AuthResponse(
        String token,
        String username,
        String role
) {
}
