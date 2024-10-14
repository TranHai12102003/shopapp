package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductAttributeDTO {
    @NotEmpty(message = "ProductId is cannot be empty")//trường product_id không được để trống
    @JsonProperty("product_id")
    private Long productId;

    @NotEmpty(message = "Attribute is name cannot be empty")//trường attribute_id không được để trống
    @JsonProperty("attribute_id")
    private Long attributeId;

    @JsonProperty("value")
    private String value;
}
