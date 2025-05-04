package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.InventoryResponseDTO;
import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.exception.CategoryNotFoundException;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.mapper.ProductMapper;
import com.ecommerce.product_service.model.Category;
import com.ecommerce.product_service.model.Product;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.ecommerce.product_service.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final RestTemplate restTemplate;
    private final String inventoryServiceUrl;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
                          RestTemplate restTemplate, @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.restTemplate = restTemplate;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    public List<ProductResponseDTO> getProductsByName(String name) {
        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        if(products.isEmpty()) {
            throw new ProductNotFoundException("No products found with this name: " + name);
        }
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    public List<ProductResponseDTO> getProductsByCategory(String categoryName) {
        Category category = categoryRepository.findByNameContainingIgnoreCase(categoryName);
        if(category == null) {
            throw new CategoryNotFoundException("No category found with this name: " + categoryName);
        }
        return category.getProducts().stream().map(ProductMapper::toDTO).toList();
    }

    public List<ProductResponseDTO> getProductsByNameAndCategory(String name, String categoryName) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseAndCategoryNameContainingIgnoreCase(name, categoryName);
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    public List<ProductResponseDTO> searchProducts(String name, String categoryName, String authHeader) {
        List<ProductResponseDTO> responseDTO;

        if(name != null && categoryName != null) {
            responseDTO = getProductsByNameAndCategory(name, categoryName);
        } else if(name != null) {
            responseDTO = getProductsByName(name);
        }else if(categoryName != null) {
            responseDTO = getProductsByCategory(categoryName);
        } else {
            responseDTO = getAllProducts();
        }

        return responseDTO;
    }

    public List<ProductResponseDTO> searchProductsWithInventory(String name, String categoryName, String authHeader) {
        List<ProductResponseDTO> responseDTO = searchProducts(name, categoryName, authHeader);

        // Return only in-stock products
        List<String> productIds = responseDTO.stream().map(ProductResponseDTO::getId).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authHeader.replace("Bearer ", ""));
        HttpEntity<List<String>> entity = new HttpEntity<>(productIds, headers);

        ResponseEntity<Map<String, InventoryResponseDTO>> response = restTemplate.exchange(inventoryServiceUrl,
                HttpMethod.POST, entity,  new ParameterizedTypeReference<Map<String, InventoryResponseDTO>>() {});
        Map<String, InventoryResponseDTO> inventoryMap = response.getBody();

        return responseDTO.stream().filter(
                product -> inventoryMap.get(product.getId()).getQuantity() > 0).toList();
    }

}
