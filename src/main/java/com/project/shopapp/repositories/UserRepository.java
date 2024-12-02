package com.project.shopapp.repositories;

import com.project.shopapp.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.*;

public interface UserRepository extends JpaRepository<User,Long> {
    //kiểm tra xem sdt đã tồn tại trong csdl chưa
    boolean existsByPhoneNumber(String phoneNumber);

    //trả về người dùng có sdt tương ứng hoặc Optional.empty()
    // để tranh trường hợp NullPointerException khi không tìm thấy bản ghi nào
    Optional<User> findByPhoneNumber(String phoneNumber);
    //SELECT * FROM users WHERE phoneNumber=?

    @Query("SELECT o FROM User o WHERE  (:keyword IS NULL OR :keyword = '' OR " +
            "o.fullName LIKE %:keyword% " +
            "OR o.address LIKE %:keyword% " +
            "OR o.phoneNumber LIKE %:keyword%) " +
            "AND LOWER(o.role.name) = 'user'")
    Page<User> findAll(@Param("keyword") String keyword, Pageable pageable);
}
