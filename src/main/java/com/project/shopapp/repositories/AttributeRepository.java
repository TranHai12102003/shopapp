package com.project.shopapp.repositories;

import com.project.shopapp.models.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttributeRepository extends JpaRepository<Attribute,Long> {
}
