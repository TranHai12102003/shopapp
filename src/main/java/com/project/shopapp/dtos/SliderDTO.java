package com.project.shopapp.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.LinkType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SliderDTO {
    @JsonProperty("title")
    private String title;

    @JsonProperty("image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @JsonProperty("link_type")
    private LinkType linkType;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("product_id")
    private Long productId;
}
