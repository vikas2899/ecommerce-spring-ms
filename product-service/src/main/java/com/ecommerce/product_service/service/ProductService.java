package com.ecommerce.product_service.service;

import com.ecommerce.product_service.dto.CategoryResponseDTO;
import com.ecommerce.product_service.dto.InventoryResponseDTO;
import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.exception.CategoryNotFoundException;
import com.ecommerce.product_service.exception.ProductNotFoundException;
import com.ecommerce.product_service.mapper.ProductMapper;
import com.ecommerce.product_service.model.Category;
import com.ecommerce.product_service.model.Product;
import com.ecommerce.product_service.model.StockStatus;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.ecommerce.product_service.repository.ProductRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public ProductResponseDTO getProductById(UUID productId) {
        Optional<Product> product = productRepository.findById(productId);
        if(product.isEmpty()) {
            throw new ProductNotFoundException("No products found with this id: " + productId);
        }

        List<String> productIds = product.stream().map(p -> p.getId().toString()).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> entity = new HttpEntity<>(productIds, headers);

        ResponseEntity<Map<String, InventoryResponseDTO>> response = restTemplate.exchange(inventoryServiceUrl,
                HttpMethod.POST, entity,  new ParameterizedTypeReference<Map<String, InventoryResponseDTO>>() {});
        Map<String, InventoryResponseDTO> inventoryMap = response.getBody();

        ProductResponseDTO responseDTO = ProductMapper.toDTO(product.get());

        if(inventoryMap.get(responseDTO.getId()).getQuantity() > 0) {
            responseDTO.setStockStatus(StockStatus.IN_STOCK);
        } else {
            responseDTO.setStockStatus(StockStatus.OUT_OF_STOCK);
        }
        return responseDTO;
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

    public List<ProductResponseDTO> getProductsByNameAndCategory(String query) {
        List<Product> products = productRepository.findByNameContainingIgnoreCaseOrCategoryNameContainingIgnoreCase(query, query);
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    public List<ProductResponseDTO> getAllProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map(ProductMapper::toDTO).toList();
    }

    public List<ProductResponseDTO> searchProducts(String query) {
        List<ProductResponseDTO> responseDTO;

        responseDTO = getProductsByNameAndCategory(query);

//        if(name != null && categoryName != null) {
//            responseDTO = getProductsByNameAndCategory(name, categoryName);
//        } else if(name != null) {
//            responseDTO = getProductsByName(name);
//        }else if(categoryName != null) {
//            responseDTO = getProductsByCategory(categoryName);
//        } else {
//            responseDTO = getAllProducts();
//        }

        return responseDTO;
    }

    public List<ProductResponseDTO> searchProductsWithInventory(String query) {
        List<ProductResponseDTO> responseDTO = searchProducts(query);

        // Return only in-stock products
        List<String> productIds = responseDTO.stream().map(ProductResponseDTO::getId).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> entity = new HttpEntity<>(productIds, headers);

        ResponseEntity<Map<String, InventoryResponseDTO>> response = restTemplate.exchange(inventoryServiceUrl,
                HttpMethod.POST, entity,  new ParameterizedTypeReference<Map<String, InventoryResponseDTO>>() {});
        Map<String, InventoryResponseDTO> inventoryMap = response.getBody();

        return responseDTO.stream().map(
                product -> {
                    if( inventoryMap.get(product.getId()).getQuantity() > 0) {
                        product.setStockStatus(StockStatus.IN_STOCK);
                    } else {
                        product.setStockStatus(StockStatus.OUT_OF_STOCK);
                    }

                    return product;
                }).toList();
    }


    public List<CategoryResponseDTO> getProductsGroupedByCategory() {

        List<Category> categories = categoryRepository.findAll();
        ObjectMapper mapper = new ObjectMapper();

        return categories.stream().map(category -> {

            List<ProductResponseDTO> products = category.getProducts().stream()
                    .map(product -> {
                        ProductResponseDTO dto = new ProductResponseDTO();
                        dto.setId(product.getId().toString());
                        dto.setName(product.getName());
                        dto.setDescription(product.getDescription());
                        dto.setPrice(product.getPrice());
                        try {
                            dto.setImages(mapper.readValue(product.getImages(), new TypeReference<List<String>>() {}));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        dto.setReviewCount(product.getReviewCount());
                        dto.setRatingCount(product.getRatingCount());
                        dto.setRating1Count(product.getRating1Count());
                        dto.setRating2Count(product.getRating2Count());
                        dto.setRating3Count(product.getRating3Count());
                        dto.setRating4Count(product.getRating4Count());
                        dto.setRating5Count(product.getRating5Count());
                        return dto;
                    }).collect(Collectors.toList());

            CategoryResponseDTO dto = new CategoryResponseDTO();
            dto.setCategoryId(category.getId());
            dto.setCategoryName(category.getName());
            dto.setProducts(products);
            return dto;
        }).collect(Collectors.toList());

    }

}
