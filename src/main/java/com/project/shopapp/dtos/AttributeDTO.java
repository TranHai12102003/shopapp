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
public class AttributeDTO {
    @NotEmpty(message = "Attribute is name cannot be empty")//trường name không được để trống
    @JsonProperty("name")
    private String name;
}
