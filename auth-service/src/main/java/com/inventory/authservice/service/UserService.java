package com.inventory.authservice.service;

import com.inventory.authservice.dto.CreateUserRequest;
import com.inventory.authservice.dto.UpdateUserRequest;
import com.inventory.authservice.dto.UserResponse;
import com.inventory.authservice.entity.AppUser;
import com.inventory.authservice.entity.Role;
import com.inventory.authservice.exception.ResourceNotFoundException;
import com.inventory.authservice.repository.AppUserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> findAll() {
        return appUserRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("El usuario ya existe");
        }

        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateUserRequest request) {
        AppUser user = getRequired(id);
        validateUsernameAvailability(request.username(), user.getId());
        validateAdminRoleChange(user, request.role());

        user.setUsername(request.username());
        user.setFullName(request.fullName());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());

        return toResponse(appUserRepository.save(user));
    }

    @Transactional
    public void deactivate(Long id) {
        AppUser user = getRequired(id);
        if (!user.isActive()) {
            return;
        }
        if (user.getRole() == Role.ROLE_ADMIN && appUserRepository.countByRoleAndActiveTrue(Role.ROLE_ADMIN) <= 1) {
            throw new IllegalArgumentException("No se puede desactivar el ultimo administrador activo");
        }
        user.setActive(false);
        appUserRepository.save(user);
    }

    @Transactional
    public void seedDefaultUsers() {
        createIfAbsent("admin", "Administrador", "Admin123!", Role.ROLE_ADMIN);
        createIfAbsent("operator", "Operador", "Operator123!", Role.ROLE_OPERATOR);
    }

    private void createIfAbsent(String username, String fullName, String password, Role role) {
        if (appUserRepository.existsByUsername(username)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setFullName(fullName);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);
        appUserRepository.save(user);
    }

    private AppUser getRequired(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private void validateUsernameAvailability(String username, Long currentUserId) {
        appUserRepository.findByUsername(username)
                .filter(existingUser -> !existingUser.getId().equals(currentUserId))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("El usuario ya existe");
                });
    }

    private void validateAdminRoleChange(AppUser user, Role newRole) {
        if (user.getRole() == Role.ROLE_ADMIN
                && newRole != Role.ROLE_ADMIN
                && user.isActive()
                && appUserRepository.countByRoleAndActiveTrue(Role.ROLE_ADMIN) <= 1) {
            throw new IllegalArgumentException("No se puede cambiar el rol del ultimo administrador activo");
        }
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
