package com.inventory.authservice.service;

import com.inventory.authservice.client.AuditClient;
import com.inventory.authservice.dto.AccessEventRequest;
import com.inventory.authservice.dto.AuthResponse;
import com.inventory.authservice.dto.LoginRequest;
import com.inventory.authservice.dto.LogoutResponse;
import com.inventory.authservice.entity.AppUser;
import com.inventory.authservice.repository.AppUserRepository;
import com.inventory.authservice.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditClient auditClient;

    public AuthService(AppUserRepository appUserRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuditClient auditClient) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditClient = auditClient;
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username())
                .filter(AppUser::isActive)
                .orElseThrow(() -> new BadCredentialsException("Credenciales invalidas"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales invalidas");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRole().name());
        auditClient.registerAccessEvent("Bearer " + token,
                new AccessEventRequest(user.getUsername(), user.getRole().name(), "LOGIN"));
        return new AuthResponse(token, user.getUsername(), user.getRole().name());
    }

    public LogoutResponse logout(Authentication authentication, String authorizationHeader) {
        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .orElse("ROLE_OPERATOR");
        auditClient.registerAccessEvent(authorizationHeader,
                new AccessEventRequest(authentication.getName(), role, "LOGOUT"));
        return new LogoutResponse("Logout registrado correctamente");
    }
}
