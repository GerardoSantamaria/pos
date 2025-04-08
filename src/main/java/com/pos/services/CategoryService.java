package com.pos.services;

import com.pos.models.Category;
import com.pos.repositories.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    private final CategoryRepository categoryRepository;

    // Constructor con inyección de dependencias en lugar de @RequiredArgsConstructor
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Obtiene todas las categorías activas
     */
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    /**
     * Obtiene todas las categorías
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Busca una categoría por ID
     */
    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    /**
     * Guarda una categoría nueva o actualiza una existente
     */
    @Transactional
    public Category saveCategory(Category category) {
        // Validar nombre único si es nueva categoría
        if (category.getId() == null && categoryRepository.existsByName(category.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + category.getName());
        }

        return categoryRepository.save(category);
    }

    /**
     * Desactiva una categoría (eliminación suave)
     */
    @Transactional
    public void deactivateCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + categoryId));

        category.setActive(false);
        categoryRepository.save(category);
    }

    /**
     * Busca categorías por nombre (búsqueda parcial)
     */
    public List<Category> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name);
    }
}