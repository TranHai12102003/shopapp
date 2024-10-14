package com.project.shopapp.repositories;

import com.project.shopapp.models.ProductAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute,Long> {
    @Query("SELECT pa FROM ProductAttribute pa JOIN FETCH pa.attribute a WHERE pa.product.id = :productId")
    List<ProductAttribute> findByProductId(Long productId);

    @Modifying
    @Query("DELETE FROM ProductAttribute pa WHERE pa.product.id = :productId")
    void deleteByProductId(@Param("productId") long productId);

}
