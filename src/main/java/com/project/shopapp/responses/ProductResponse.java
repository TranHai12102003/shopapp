package com.project.shopapp.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.shopapp.models.Attribute;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductAttribute;
import com.project.shopapp.models.ProductImage;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse extends BaseResponse {
    private Long id;
    private String name;
    private Float price;
    private String thumbnail;
    private String description;

    @JsonProperty("product_images")
    private List<ProductImage> productImages=new ArrayList<>();

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("product_attributes")
    private List<ProductAttribute> productAttributes=new ArrayList<>();

    @JsonProperty("attributes")
    private List<Attribute> attributes=new ArrayList<>();

    public static ProductResponse fromProduct(Product product){
        ProductResponse productResponse = ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .thumbnail(product.getThumbnail())
                .description(product.getDescription())
                .categoryId(product.getCategory().getId())
                .productImages(product.getProductImages())
                .productAttributes(product.getProductAttributes()) // ánh xạ trực tiếp
                .build();

        // Đảm bảo `ProductAttribute` không null và ánh xạ đầy đủ
        List<Attribute> attributes = product.getProductAttributes().stream()
                .filter(productAttribute -> productAttribute.getAttribute() != null)
                .map(productAttribute -> {
                    Attribute attribute = new Attribute();
                    attribute.setId(productAttribute.getAttribute().getId());
                    attribute.setName(productAttribute.getAttribute().getName());
                    attribute.setProductAttributes(Collections.singletonList(productAttribute));
                    return attribute;
                })
                .distinct()
                .collect(Collectors.toList());

        productResponse.setAttributes(attributes);
        productResponse.setCreatedAt(product.getCreatedAt());
        productResponse.setUpdatedAt(product.getUpdatedAt());

        return productResponse;
    }

}
