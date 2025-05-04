package com.ecommerce.product_service.repository;

import com.ecommerce.product_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Category findByNameContainingIgnoreCase(String name);
}
