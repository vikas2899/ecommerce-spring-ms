package com.ecommerce.product_service.mapper;

import com.ecommerce.product_service.dto.ProductResponseDTO;
import com.ecommerce.product_service.model.Product;

public class ProductMapper {

    public static ProductResponseDTO toDTO(Product product) {
        ProductResponseDTO responseDTO = new ProductResponseDTO();

        responseDTO.setId(product.getId().toString());
        responseDTO.setDescription(product.getDescription());
        responseDTO.setName(product.getName());
        responseDTO.setPrice(product.getPrice());
        responseDTO.setCategoryName(product.getCategory().getName());

        return responseDTO;
    }

}
