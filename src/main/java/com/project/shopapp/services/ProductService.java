package com.project.shopapp.services;

import com.project.shopapp.components.LocalizationUtils;
import com.project.shopapp.dtos.ProductDTO;
import com.project.shopapp.dtos.ProductImageDTO;
import com.project.shopapp.exceptions.DataNotFoundException;
import com.project.shopapp.exceptions.InvalidParamException;
import com.project.shopapp.models.Category;
import com.project.shopapp.models.Product;
import com.project.shopapp.models.ProductImage;
import com.project.shopapp.repositories.CategoryRepository;
import com.project.shopapp.repositories.ProductImageRepository;
import com.project.shopapp.repositories.ProductRepository;
import com.project.shopapp.responses.ProductResponse;
import com.project.shopapp.utils.MessageKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final LocalizationUtils localizationUtils;

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
        return productRepository.save(newProduct);
    }

    @Override
    public Product getProductById(long productId) throws Exception {
        Optional<Product> optionalProduct = productRepository.getDetailProduct(productId);
        if (optionalProduct.isPresent()) {
            return optionalProduct.get();
        }
        throw new DataNotFoundException("Cannot find product with id="+productId);
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
    @Transactional
    public Product updateProduct(
            long id,
            ProductDTO productDTO) throws Exception {
        Product existingProduct= getProductById(id);
        if(existingProduct!= null){
            //copy các thuộc tính từ DTO ->Product
            //Có thể sử dụng modelMapper
            Category existingCategory= categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(()->new DataNotFoundException(
                            localizationUtils.getLocalizedMessage(MessageKeys.CATEGORY_NOT_FOUND)+productDTO.getCategoryId()));
            existingProduct.setName(productDTO.getName());
            existingProduct.setCategory(existingCategory);
            existingProduct.setPrice(productDTO.getPrice());
            existingProduct.setDescription(productDTO.getDescription());
            existingProduct.setThumbnail(productDTO.getThumbnail());
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
        ProductImage newProductImage=ProductImage.builder()
                .product(existingProduct)
                .imageUrl(productImageDTO.getImageUrl())
                .build();
        //không cho thêm quá 5 ảnh cho 1 sản phẩm
        int size=productImageRepository.findByProductId(productId).size();
        if(size>=ProductImage.MAXIMUM_IMAGES_PER_PRODUCT){
            throw new InvalidParamException("Number of images must be <="+ProductImage.MAXIMUM_IMAGES_PER_PRODUCT);
        }
        return  productImageRepository.save(newProductImage);
    }



}
