package com.project.shopapp.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "attributes")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public class Attribute {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @Column(name = "name",nullable = false)
        @JsonProperty("name")
        private String name;

        @OneToMany(mappedBy = "attribute", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
        @JsonProperty("product_attributes")
        private List<ProductAttribute> productAttributes=new ArrayList<>();
    }
