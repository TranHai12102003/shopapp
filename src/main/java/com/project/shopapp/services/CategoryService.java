package com.project.shopapp.services;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final LocalizationUtils localizationUtils;

    @Override
    public Category createCategory(CategoryDTO categoryDTO) {
        Category newCategory=Category
                .builder()
                .name(categoryDTO.getName())
                .build();
        return categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(()->new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND)));
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Category updateCategory(long categoryId,  CategoryDTO categoryDTO) throws DataNotFoundException {
        Category existingCategory=getCategoryById(categoryId);
        if(existingCategory==null){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND));
        }
        existingCategory.setName(categoryDTO.getName());
        categoryRepository.save(existingCategory);
        return existingCategory;
    }

    @Override
    public void deleteCategory(long id) {
        //xoa cung
        categoryRepository.deleteById(id);
    }
}
