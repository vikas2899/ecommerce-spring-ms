package com.ecommerce.order_service.dto;

import java.util.List;

public class GetOrderResponseDTO {
    private String id;
    private List<CartProductsDTO> products;
    private String orderStatus;
    private String createdAt;

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

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
