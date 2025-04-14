package com.pos.services.core;

import com.pos.models.core.User;
import com.pos.models.core.Role;
import com.pos.repositories.core.UserRepository;
import com.pos.repositories.core.RoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing User entities.
 * Implements UserDetailsService for Spring Security integration.
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructor with dependencies.
     *
     * @param userRepository The user repository
     * @param roleRepository The role repository
     * @param passwordEncoder The password encoder
     */
    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Loads user details by username for authentication.
     *
     * @param username The username
     * @return The UserDetails
     * @throws UsernameNotFoundException If the user is not found
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }

    /**
     * Creates a new user with the provided details.
     *
     * @param user The user to create
     * @param roleNames The roles to assign
     * @return The created user
     */
    @Transactional
    public User createUser(User user, String... roleNames) {
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Assign roles
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        // Save user
        return userRepository.save(user);
    }

    /**
     * Updates an existing user.
     *
     * @param user The user to update
     * @return The updated user
     */
    @Transactional
    public User updateUser(User user) {
        // Get existing user
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + user.getId()));

        // Update fields
        existingUser.setUsername(user.getUsername());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        existingUser.setEnabled(user.isEnabled());

        // Save updated user
        return userRepository.save(existingUser);
    }

    /**
     * Finds a user by their ID.
     *
     * @param id The user ID
     * @return An Optional containing the user if found
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Finds a user by their username.
     *
     * @param username The username
     * @return An Optional containing the user if found
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Gets all users.
     *
     * @return List of all users
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The user ID
     */
    @Transactional
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Authenticates a user with the provided credentials.
     *
     * @param username The username
     * @param password The raw password
     * @return True if authentication succeeds, false otherwise
     */
    public boolean authenticate(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        return user.isEnabled() && passwordEncoder.matches(password, user.getPassword());
    }
}