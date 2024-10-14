package com.project.shopapp.repositories;

import com.project.shopapp.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;


public interface CategoryRepository extends JpaRepository<Category,Long> {
        List<Category> findByParentIsNull(); // Lấy danh sách danh mục cha

        List<Category> findByParentId(Long parentId);

}
