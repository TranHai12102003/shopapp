package com.project.shopapp.controllers;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.responses.CategoryResponse;
import com.project.shopapp.services.CategoryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.LocaleResolver;

import java.util.List;
import java.util.Locale;

@RestController//khai bao controller
@RequestMapping("${api.prefix}/categories")//đường dẫn
//@Validated
// anotation dùng để xác minh rằng một trường nào đó không được để trống
//dependency Injection
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    @PostMapping("")
    //Nếu tham số truyền vào là 1 object thì sao?=> Data Transfer Object= Request Object
    public  ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result
            ,HttpServletRequest request){
        Locale locale=localeResolver.resolveLocale(request);
        //kiểm tra xem có lỗi hay không nếu có sẽ trả về cho client lỗi
        if(result.hasErrors()){
            //nếu tìm thấy lỗi stream() sẽ duyệt qua các lỗi đó
            //và lấy ra lỗi cần thiết sau đó "map()" ánh xạ sang 1 mảng khác và trả về lỗi đó
            List<String> errorMessages= result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(CategoryResponse.builder()
                            .message(messageSource.getMessage(
                                    "category.create_category.create_failed"
                                    ,null
                                    ,locale))
                            .build());
        }
            categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok(CategoryResponse.builder()
                        .message(messageSource.getMessage("category.create_category.create_successfully",null,locale))
                .build());
    }

    @GetMapping("")//http://localhost:8080/api/v1/categories?page=16&limit=10
    public ResponseEntity<List<Category>> getAllCategories(
            //@RequestParam("page") đây là tham số ở client
            //int page đây là tham số của server
            @RequestParam("page") int page,
            @RequestParam("limit") int limit)
    {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }


    @PutMapping("/{id}")
    public  ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long id
            , @Valid @RequestBody CategoryDTO categoryDTO
            , HttpServletRequest request){
        categoryService.updateCategory(id, categoryDTO);
        Locale locale=localeResolver.resolveLocale(request);
        return ResponseEntity.ok(CategoryResponse.builder()
                        .message(messageSource.getMessage("category.update_category.update_successfully",null,locale))
                .build());
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Delete Category witd id = "+ id + " successfully ");
    }
}
