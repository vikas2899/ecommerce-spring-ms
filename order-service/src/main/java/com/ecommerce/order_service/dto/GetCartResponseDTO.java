package com.ecommerce.order_service.dto;

import java.util.List;

public class GetCartResponseDTO {
    private String id;
    private List<CartProductsDTO> products;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<CartProductsDTO> getProducts() {
        return products;
    }

    public void setProducts(List<CartProductsDTO> products) {
        this.products = products;
    }
}
