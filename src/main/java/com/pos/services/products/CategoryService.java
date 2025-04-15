package com.pos.services.products;

import com.pos.dtos.products.CategoryDTO;
import com.pos.mapper.products.CategoryMapper;
import com.pos.models.products.Category;
import com.pos.repositories.products.CategoryRepository;
import org.hibernate.ObjectNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = this.categoryRepository.findAll();
        return CategoryMapper.INSTANCE.toDtoList(categories);
    }

    public List<CategoryDTO> getCategoriesByNameContaining(String name) {
        List<Category> categories = this.categoryRepository.findByNameContaining(name);
        return CategoryMapper.INSTANCE.toDtoList(categories);
    }

    @Transactional
    public CategoryDTO updateCategory(CategoryDTO categoryDTO){
        if(!this.categoryRepository.existsById(categoryDTO.getId())) {
            throw new ObjectNotFoundException("Category not found by id", categoryDTO.getId());
        };

        Category category = CategoryMapper.INSTANCE.toEntity(categoryDTO);
        category = this.categoryRepository.save(category);

        return  CategoryMapper.INSTANCE.toDto(category);
    }

    @Transactional
    public void deleteCategoryById(Long id) {
        this.categoryRepository.deleteById(id);
    }

    @Transactional
    public CategoryDTO createCategory(CategoryDTO categoryDTO) throws RuntimeException {
        this.categoryRepository.findByName(categoryDTO.getName())
                .ifPresent( category -> { throw new RuntimeException("Ya exsite una categoria con ese nombre");});

        Category category = CategoryMapper.INSTANCE.toEntity(categoryDTO);
        return CategoryMapper.INSTANCE.toDto(this.categoryRepository.save(category));
    }

    @Transactional
    public CategoryDTO updateCategoryActiveById(Long id) throws Exception {
        Category category =  this.categoryRepository.findById(id)
                                .orElseThrow(() ->new Exception("No se encontro categoria con id: " + id));

        category.setActive(!category.isActive());
        this.categoryRepository.save(category);
        return CategoryMapper.INSTANCE.toDto(category);
    }
}
