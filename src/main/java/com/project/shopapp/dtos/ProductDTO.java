package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data//toString
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductDTO {
    @NotBlank(message = "Tên không được để trống")
    @Size(min = 3,max = 200,message = "Tên phải dài từ 3 đến 200 ký tự")
    private String name;

    @Min(value = 0,message = "Giá phải lớn hơn hoặc bằng 0")
    @Max(value = 10000000,message = "Giá phải nhỏ hơn hoặc bằng 10.000.000")
    private Float price;

    private String thumbnail;
    private String description;

    @JsonProperty("category_id")
    private Long categoryId;


}
