package com.ecommerce.product_service.controller;

import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> getProducts(@RequestParam(required = false) String name,
                                                                @RequestParam(required = false) String category) {

        List<ProductResponseDTO> products = productService.searchProducts(name, category);
        return ResponseEntity.ok().body(products);
    }

}
