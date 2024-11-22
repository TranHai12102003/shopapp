package com.project.shopapp.services;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.models.Category;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {
    private final CategoryRepository categoryRepository;
    private final LocalizationUtils localizationUtils;
    private final ModelMapper modelMapper;


    @Override
    @Transactional
    public Category createCategory(CategoryDTO categoryDTO) {
//        Category newCategory=Category
//                .builder()
//                .name(categoryDTO.getName())
//                .build();
        Category newCategory=new Category();
        modelMapper.typeMap(CategoryDTO.class,Category.class)
                .addMappings(mapper->mapper.skip(Category::setId));
        modelMapper.map(categoryDTO,newCategory);
        return categoryRepository.save(newCategory);
    }

    @Override
    public Category getCategoryById(long id) {
        return categoryRepository.findById(id)
                .orElseThrow(()->new RuntimeException(localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND)));
    }

    @Override
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public List<Category> getAllCategoriesNoPage() {
        return categoryRepository.findAll();
    }


    @Override
    @Transactional
    public Category updateCategory(long categoryId,  CategoryDTO categoryDTO) throws DataNotFoundException {
        Category existingCategory=getCategoryById(categoryId);
        if(existingCategory==null){
            throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND));
        }
        existingCategory.setName(categoryDTO.getName());
        existingCategory.setParentId(categoryDTO.getParentId());
        categoryRepository.save(existingCategory);
        return existingCategory;
    }

    @Override
    @Transactional
    public void deleteCategory(long id) {
        //xoa cung
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> getSubCategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    public List<Category> getParentCategories() {
        return categoryRepository.findByParentIsNull();
    }
}
