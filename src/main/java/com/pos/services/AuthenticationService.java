package com.pos.services;

import com.pos.models.User;
import com.pos.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public AuthenticationService(UserRepository userRepository, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Autentica un usuario con su nombre y contraseña
     * @return true si la autenticación fue exitosa
     */
    public boolean authenticate(String username, String password) {
        try {
            // Crear token de autenticación
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Autenticar mediante el AuthenticationManager de Spring Security
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Establecer la autenticación en el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);

            LOGGER.info("Usuario autenticado: {}", username);
            return true;
        } catch (AuthenticationException e) {
            LOGGER.warn("Fallo de autenticación para el usuario: {}", username);
            return false;
        }
    }

    /**
     * Obtiene el usuario actualmente autenticado
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        SecurityContextHolder.clearContext();
        LOGGER.info("Usuario desconectado");
    }
}