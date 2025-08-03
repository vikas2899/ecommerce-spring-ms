package com.ecommerce.product_service.mapper;

import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.model.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ProductMapper {

    public static ProductResponseDTO toDTO(Product product) {
        ProductResponseDTO responseDTO = new ProductResponseDTO();

        ObjectMapper mapper = new ObjectMapper();

        responseDTO.setId(product.getId().toString());
        responseDTO.setDescription(product.getDescription());
        responseDTO.setName(product.getName());
        responseDTO.setPrice(product.getPrice());
        responseDTO.setCategoryName(product.getCategory().getName());
        try {
            responseDTO.setImages(mapper.readValue(product.getImages(), new TypeReference<List<String>>() {}));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        responseDTO.setRatingCount(product.getRatingCount());
        responseDTO.setReviewCount(product.getReviewCount());
        responseDTO.setRating1Count(product.getRating1Count());
        responseDTO.setRating2Count(product.getRating2Count());
        responseDTO.setRating3Count(product.getRating3Count());
        responseDTO.setRating4Count(product.getRating4Count());
        responseDTO.setRating5Count(product.getRating5Count());

        return responseDTO;
    }

}
