package com.project.shopapp.services;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ICategoryService {
    Category createCategory(CategoryDTO category);

    Category getCategoryById(long id);
    Page<Category> getAllCategories(Pageable pageable);
    List<Category> getAllCategoriesNoPage();

    Category updateCategory(long categoryId, CategoryDTO category) throws DataNotFoundException;

    void deleteCategory(long id);
//    List<Category> getParentCategories();
     List<Category> getSubCategories(Long parentId);
     List<Category> getParentCategories();
}
