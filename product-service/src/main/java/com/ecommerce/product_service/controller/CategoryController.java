package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.CategoryResponseDTO;
import com.ecommerce.product_service.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/category")
public class CategoryController {

    CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getProductsByCategory(@PathVariable("id") UUID categoryId) {
            try {
                CategoryResponseDTO response =  categoryService.getCategoryData(categoryId);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.badRequest().build();
            }
    }

}
