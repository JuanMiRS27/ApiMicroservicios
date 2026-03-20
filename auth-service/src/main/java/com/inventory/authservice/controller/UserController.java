package com.inventory.authservice.controller;

import com.inventory.authservice.dto.CreateUserRequest;
import com.inventory.authservice.dto.UpdateUserRequest;
import com.inventory.authservice.dto.UserResponse;
import com.inventory.authservice.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> findAll(Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userService.findAll());
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request,
                                               Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable("id") Long id,
                                               @Valid @RequestBody UpdateUserRequest request,
                                               Authentication authentication) {
        requireAdmin(authentication);
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable("id") Long id, Authentication authentication) {
        requireAdmin(authentication);
        userService.deactivate(id);
        return ResponseEntity.noContent().build();
    }

    private void requireAdmin(Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new org.springframework.security.access.AccessDeniedException("Solo admin puede gestionar usuarios");
        }
    }
}
