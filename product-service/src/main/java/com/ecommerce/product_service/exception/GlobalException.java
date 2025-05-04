package com.ecommerce.product_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalException {

    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleCategoryNotFoundException(CategoryNotFoundException ex) {

        log.warn("category not found exception: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Category with given name not found");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException ex) {

        log.warn("product not found exception: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Product with given name not found");
        return ResponseEntity.badRequest().body(errors);
    }

}
