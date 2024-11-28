package com.project.shopapp.repositories;

import com.project.shopapp.models.Product;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product,Long> {
    boolean existsByName(String name);

    Page<Product> findAll(Pageable pageable);//phân trang

    //tìm kiếm san pham theo ten va danh muc
    @Query("SELECT p FROM Product p WHERE " +
            "(:categoryId IS NULL OR :categoryId = 0 OR p.category.id = :categoryId OR p.category.parentId = :categoryId) " +
            "AND (:keyword IS NULL OR :keyword = '' OR (p.name LIKE %:keyword% OR p.description LIKE %:keyword%))")
    Page<Product> searchProducts
            (@Param("categoryId") Long categoryId,
             @Param("keyword") String keyword, Pageable pageable);

    //lay ra san pham va va hinh anh cua san pham do
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.productImages WHERE p.id = :productId")
    Optional<Product> getDetailProduct(@Param("productId") Long productId);

    // lay danh sach san pham tu danh sach id
    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findProductsByIds(@Param("productIds")List<Long> productIds);

    //lấy sản phẩm thuộc 1 danh muc
    Page<Product> findByCategoryId(Long categoryId,Pageable pageable);

    List<Product> findTop4ByOrderByIdDesc();
}
