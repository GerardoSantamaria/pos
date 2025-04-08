package com.pos.repositories;

import com.pos.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Buscar categorías activas
    List<Category> findByActiveTrue();

    // Buscar categorías por nombre (parcial)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Verificar si existe una categoría con un nombre específico
    boolean existsByName(String name);
}