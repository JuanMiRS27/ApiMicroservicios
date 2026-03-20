package com.inventory.inventoryservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class InternalApiKeyFilter extends OncePerRequestFilter {

    public static final String INTERNAL_API_KEY_HEADER = "X-Internal-Api-Key";

    private final String internalApiKey;
    private final SecurityErrorHandler securityErrorHandler;

    public InternalApiKeyFilter(@Value("${app.security.internal-api-key:inventory-internal-key}") String internalApiKey,
                                SecurityErrorHandler securityErrorHandler) {
        this.internalApiKey = internalApiKey;
        this.securityErrorHandler = securityErrorHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/internal/")
                && !internalApiKey.equals(request.getHeader(INTERNAL_API_KEY_HEADER))) {
            securityErrorHandler.write(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                    "Endpoint interno: acceso denegado");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
