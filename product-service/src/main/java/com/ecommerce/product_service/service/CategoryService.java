package com.ecommerce.product_service.service;
import com.ecommerce.product_service.dto.CategoryResponseDTO;
import com.ecommerce.product_service.dto.InventoryResponseDTO;
import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.model.Category;
import com.ecommerce.product_service.model.Product;
import com.ecommerce.product_service.model.StockStatus;
import com.ecommerce.product_service.repository.CategoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    CategoryRepository categoryRepository;
    String inventoryServiceUrl;
    RestTemplate restTemplate;

    public CategoryService(CategoryRepository categoryRepository,  @Value("${inventory.service.url}") String inventoryServiceUrl, RestTemplate restTemplate) {
        this.categoryRepository = categoryRepository;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.restTemplate = restTemplate;
    }

    public CategoryResponseDTO getCategoryData(UUID categoryId) throws Exception {
        Optional<Category> data = categoryRepository.findById(categoryId);
        if(data.isEmpty()) {
            throw new Exception("Invalid Category Id");
        }

        Category category = data.get();
        CategoryResponseDTO responseDTO = new CategoryResponseDTO();
        responseDTO.setCategoryId(category.getId());
        responseDTO.setCategoryName(category.getName());

        ObjectMapper mapper = new ObjectMapper();
        List<String> productIds = category.getProducts().stream().map(product -> product.getId().toString()).toList();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> entity = new HttpEntity<>(productIds, headers);

        ResponseEntity<Map<String, InventoryResponseDTO>> response = restTemplate.exchange(inventoryServiceUrl,
                HttpMethod.POST, entity,  new ParameterizedTypeReference<Map<String, InventoryResponseDTO>>() {});
        Map<String, InventoryResponseDTO> inventoryMap = response.getBody();

        List<ProductResponseDTO> products = category.getProducts().stream()
                .map(product -> {
                    ProductResponseDTO dto = new ProductResponseDTO();
                    dto.setId(product.getId().toString());
                    dto.setName(product.getName());
                    dto.setDescription(product.getDescription());
                    dto.setCategoryName(category.getName());
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

                    if( inventoryMap.get(product.getId().toString()).getQuantity() > 0) {
                        dto.setStockStatus(StockStatus.IN_STOCK);
                    } else {
                        dto.setStockStatus(StockStatus.OUT_OF_STOCK);
                    }

                    return dto;
                }).toList();


        responseDTO.setProducts(products);
        return responseDTO;
    }

}
