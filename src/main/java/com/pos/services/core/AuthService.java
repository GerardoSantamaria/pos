package com.pos.services.core;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Service for handling authentication operations.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    /**
     * Constructor with dependencies.
     *
     * @param authenticationManager The authentication manager
     * @param userService The user service
     */
    public AuthService(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username The username
     * @param password The password
     * @return The authenticated user details if successful
     * @throws AuthenticationException If authentication fails
     */
    public UserDetails authenticate(String username, String password) throws AuthenticationException {
        // Create authentication token
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, password);

        // Authenticate
        Authentication authenticated = authenticationManager.authenticate(authentication);

        // Set authentication in security context
        SecurityContextHolder.getContext().setAuthentication(authenticated);

        // Return user details
        return (UserDetails) authenticated.getPrincipal();
    }

    /**
     * Logs out the current user.
     */
    public void logout() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Gets the currently authenticated user.
     *
     * @return The authenticated user details or null if not authenticated
     */
    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return (UserDetails) authentication.getPrincipal();
    }

    /**
     * Checks if the current user has the specified role.
     *
     * @param role The role to check
     * @return True if the user has the role, false otherwise
     */
    public boolean hasRole(String role) {
        UserDetails userDetails = getCurrentUser();
        if (userDetails == null) {
            return false;
        }

        return userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }
}