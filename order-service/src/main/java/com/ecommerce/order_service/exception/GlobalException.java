package com.ecommerce.order_service.exception;

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

    @ExceptionHandler(EmptyCartException.class)
    public ResponseEntity<Map<String, String>> handleEmptyCartException(EmptyCartException ex) {

        log.warn("Cart is empty: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Please add some products to cart first");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<Map<String, String>> handleCartException(CartException ex) {

        log.warn("Cart not found {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Cart not found");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleProductNotFoundException(ProductNotFoundException ex) {

        log.warn("Error calling product service {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Invalid products inside cart");
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ProductOutOfStockException.class)
    public ResponseEntity<Map<String, String>> handleProductOutOfStockException(ProductOutOfStockException ex) {

        log.warn("Product out of stock response from inventory {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        errors.put("message", "Some products inside your cart are out of stock. Please try again later.");
        return ResponseEntity.badRequest().body(errors);
    }

}
