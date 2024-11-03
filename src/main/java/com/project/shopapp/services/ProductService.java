package com.project.shopapp.services;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ProductAttributeDTO;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.*;
import com.project.shopapp.repositories.*;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final LocalizationUtils localizationUtils;
    private final AttributeRepository attributeRepository;
    private  final ProductAttributeRepository productAttributeRepository;

    @Override
    @Transactional
    public Product createProduct(ProductDTO productDTO) throws DataNotFoundException {
       Category existingCategory= categoryRepository
                .findById(productDTO.getCategoryId())
                .orElseThrow(()->
                        new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND)));
        Product newProduct=Product.builder()
                .name(productDTO.getName())
                .price(productDTO.getPrice())
                .thumbnail(productDTO.getThumbnail())
                .description(productDTO.getDescription())
                .category(existingCategory)
                .build();
        Product saveProduct= productRepository.save(newProduct);
        // them vao product_attributes
        if(productDTO.getProductAttributeDTOS()!=null && !productDTO.getProductAttributeDTOS().isEmpty()){
            List<ProductAttribute> productAttributes=productDTO.getProductAttributeDTOS().stream()
                    .map(productAttributeDTO -> {
                        Attribute attribute=attributeRepository.findById(productAttributeDTO.getAttributeId())
                                .orElseThrow(()-> new DataNotFoundException
                                        ("Attribute not found with id: "+productAttributeDTO.getAttributeId()));
                        return ProductAttribute.builder()
                                .product(saveProduct)
                                .attribute(attribute)
                                .value(productAttributeDTO.getValue())
                                .build();
                    })
                    .collect(Collectors.toList());
            productAttributeRepository.saveAll(productAttributes);
        }
        return saveProduct;
    }

    @Override
    public Product getProductById(long productId) throws Exception {
        Optional<Product> optionalProduct = productRepository.getDetailProduct(productId);
        if (optionalProduct.isPresent()) {
            Product product= optionalProduct.get();
            //lấy danh sách các thuộc tính của sản phẩm
            List<ProductAttribute> productAttributes=productAttributeRepository.findByProductId(productId);

            //gan danh sach cac thuoc tinh vao doi tuong Product
            product.setProductAttributes(productAttributes);
            return product;

        }
        throw new DataNotFoundException(localizationUtils.getLocalizedMessage(MessageKeys.CAN_NOT_FIND_PRODUCTS,productId));
    }
    public Page<ProductResponse> getAllProducts(String keyword,Long categoryId,PageRequest pageRequest) {
        //lấy danh sách các sản phẩm theo trang(page) và giới hạn(limit) và categoryId nếu có
        // anh xa tu Product sang ProductResponse
        Page<Product> productsPage;
        productsPage=productRepository.searchProducts(categoryId,keyword,pageRequest);
        return  productsPage.map(ProductResponse::fromProduct);
        //dung biểu thúc lamda để tham chiếu đến phương thước static của ProductResponse
    }

    @Override
    public List<Product> findProductsByIds(List<Long> productIds) {
        return productRepository.findProductsByIds(productIds);
    }

    @Override
    public Page<ProductResponse> getProductsByCategoryId(Long categoryId, PageRequest pageRequest) {
        Page<Product> productsPage;
        productsPage=productRepository.findByCategoryId(categoryId,pageRequest);
        return productsPage.map(ProductResponse::fromProduct);
    }


    @Override
    @Transactional
    public Product updateProduct(long id, ProductDTO productDTO) throws Exception {
        Product existingProduct = getProductById(id);
        if (existingProduct != null) {
            // Cập nhật các thuộc tính từ DTO vào Product
            Category existingCategory = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new DataNotFoundException(
                            localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND) + productDTO.getCategoryId()));

            existingProduct.setName(productDTO.getName());
            existingProduct.setCategory(existingCategory);
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setThumbnail(productDTO.getThumbnail());

            // Xóa các product attributes cũ
            productAttributeRepository.deleteByProductId(existingProduct.getId());

            // Thêm mới các product attributes từ DTO
            if (productDTO.getProductAttributeDTOS() != null && !productDTO.getProductAttributeDTOS().isEmpty()) {
                List<ProductAttribute> productAttributes = productDTO.getProductAttributeDTOS().stream()
                        .map(productAttributeDTO -> {
                            Attribute attribute = attributeRepository
                                    .findById(productAttributeDTO.getAttributeId())
                                    .orElseThrow(() -> new DataNotFoundException(
                                            "Attribute not found with id: "
                                                    + productAttributeDTO.getAttributeId()));
                            return ProductAttribute.builder()
                                    .product(existingProduct)
                                    .attribute(attribute)
                                    .value(productAttributeDTO.getValue())
                                    .build();
                        })
                        .collect(Collectors.toList());
                productAttributeRepository.saveAll(productAttributes);
            }

            return productRepository.save(existingProduct);
        }
        return null;
    }


    @Override
    @Transactional
    public void deleteProduct(long id) {
       Optional<Product> optionalProduct= productRepository.findById(id);
        optionalProduct.ifPresent(productRepository::delete);
    }

    @Override
    public boolean existsByName(String name) {
        return productRepository.existsByName(name);
    }

    @Override
    @Transactional
    public ProductImage createProductImage(
            Long productId,
            ProductImageDTO productImageDTO) throws Exception {
        Product existingProduct= productRepository.findById(productId)
                .orElseThrow(()->new DataNotFoundException(
                        "Cannot find product with id : "+ productImageDTO.getProductId()));
        //không cho thêm quá 5 ảnh cho 1 sản phẩm
        int size=productImageRepository.findByProductId(productId).size();
        if(size>=ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new InvalidParamException("Number of images must be <="+ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        // Tạo mới ProductImage và lưu vào cơ sở dữ liệu
        ProductImage newProductImage=ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        // Lưu ProductImage vào cơ sở dữ liệu và nhận id tự động sinh ra
        newProductImage = productImageRepository.save(newProductImage);

        if(existingProduct.getThumbnail()==null || existingProduct.getThumbnail().isEmpty()){
            existingProduct.setThumbnail(newProductImage.getImageUrl());
            productRepository.save(existingProduct);
        }

        return  newProductImage;
    }



}
