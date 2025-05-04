package com.ecommerce.inventory_service.exception;

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

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(Exception ex) {

         log.warn("product not found in inventory exception: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Product with given id not found in inventory");
        return ResponseEntity.badRequest().body(errors);
    }

}
