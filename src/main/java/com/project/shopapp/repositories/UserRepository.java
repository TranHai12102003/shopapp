package com.project.shopapp.repositories;

import com.project.shopapp.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.*;

public interface UserRepository extends JpaRepository<User,Long> {
    //kiểm tra xem sdt đã tồn tại trong csdl chưa
    boolean existsByPhoneNumber(String phoneNumber);

    //trả về người dùng có sdt tương ứng hoặc Optional.empty()
    // để tranh trường hợp NullPointerException khi không tìm thấy bản ghi nào
    Optional<User> findByPhoneNumber(String phoneNumber);
    //SELECT * FROM users WHERE phoneNumber=?
}
