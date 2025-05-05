package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.service.ProductService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
public class ProductController {

    private static final Logger log = LoggerFactory.getLogger(ProductController.class);
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @CircuitBreaker(name = "product-inventory-breaker", fallbackMethod = "productInventoryFallback")
    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> getProducts(@RequestParam(required = false) String name,
                                                                @RequestParam(required = false) String category,
                                                                HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ProductResponseDTO> products = productService.searchProductsWithInventory(name, category, authHeader);
        return ResponseEntity.ok().body(products);
    }

    public ResponseEntity<List<ProductResponseDTO>> productInventoryFallback(@RequestParam(required = false) String name,
                                                                             @RequestParam(required = false) String category,
                                                                             HttpServletRequest request,
                                                                             Exception ex) {
        log.info("Fallback: Inventory service not available... {}", ex.getMessage());

        String authHeader = request.getHeader("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<ProductResponseDTO> products = productService.searchProducts(name, category, authHeader);
        return ResponseEntity.ok().body(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable("id") UUID productId, @RequestHeader("Authorization") String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ProductResponseDTO productResponseDTO = productService.getProductById(productId);
        return ResponseEntity.ok().body(productResponseDTO);
    }

}
