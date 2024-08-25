package com.project.shopapp.dtos;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

//thư viện lambok hỗ trợ getter và setter không cân phải gõ lại
@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryDTO {
    @NotEmpty(message = "Category is name cannot be empty")//trường name không được để trống
    private String name;
}
