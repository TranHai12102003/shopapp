package com.project.shopapp.controllers;

import com.project.shopapp.dtos.CategoryDTO;
import com.project.shopapp.models.Category;
import com.project.shopapp.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController//khai bao controller
@RequestMapping("${api.prefix}/categories")//đường dẫn
//@Validated
// anotation dùng để xác minh rằng một trường nào đó không được để trống
//dependency Injection
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("")
    //Nếu tham số truyền vào là 1 object thì sao?=> Data Transfer Object= Request Object
    public  ResponseEntity<?> createCategory(
            @Valid @RequestBody CategoryDTO categoryDTO,
            BindingResult result){
        //kiểm tra xem có lỗi hay không nếu có sẽ trả về cho client lỗi
        if(result.hasErrors()){
            //nếu tìm thấy lỗi stream() sẽ duyệt qua các lỗi đó
            //và lấy ra lỗi cần thiết sau đó "map()" ánh xạ sang 1 mảng khác và trả về lỗi đó
            List<String> errorMessages= result.getFieldErrors()
                    .stream()
                    .map(FieldError::getDefaultMessage)
                    .toList();
            return ResponseEntity.badRequest().body(errorMessages);
        }
            categoryService.createCategory(categoryDTO);
        return ResponseEntity.ok("Insert category successfully");
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
    public  ResponseEntity<String> updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryDTO categoryDTO){
        categoryService.updateCategory(id, categoryDTO);
        return ResponseEntity.ok("Update category successfully");
    }

    @DeleteMapping("/{id}")
    public  ResponseEntity<String> deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("Delete Category witd id = "+ id + " successfully ");
    }
}
