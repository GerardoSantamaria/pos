package com.pos.mapper.products;

import com.pos.dtos.products.CategoryDTO;
import com.pos.models.products.Category;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {

    CategoryMapper INSTANCE = Mappers.getMapper(CategoryMapper.class);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "description", target = "description")
    @Mapping(source = "active", target = "active")
    CategoryDTO toDto(Category category);

    @InheritInverseConfiguration
    Category toEntity(CategoryDTO categoryDTO);

    List<CategoryDTO> toDtoList(List<Category> categories);
    List<Category> toEntityList(List<CategoryDTO> categoryDTOS);
}
