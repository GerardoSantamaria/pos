package com.pos.repositories.core;

import com.pos.models.core.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for accessing and manipulating User entities.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their username.
     *
     * @param username The username to search for
     * @return An Optional containing the user if found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks if a user with the given username exists.
     *
     * @param username The username to check
     * @return True if a user with the username exists, false otherwise
     */
    boolean existsByUsername(String username);
}