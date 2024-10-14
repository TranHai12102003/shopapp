package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("parent_id")
    private Long parentId;
}
