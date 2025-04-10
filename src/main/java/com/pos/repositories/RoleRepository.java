package com.pos.repositories;

import com.pos.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repository for accessing and manipulating Role entities.
 */
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Finds a role by its name.
     *
     * @param name The role name to search for
     * @return An Optional containing the role if found
     */
    Optional<Role> findByName(String name);

    /**
     * Checks if a role with the given name exists.
     *
     * @param name The role name to check
     * @return True if a role with the name exists, false otherwise
     */
    boolean existsByName(String name);
}