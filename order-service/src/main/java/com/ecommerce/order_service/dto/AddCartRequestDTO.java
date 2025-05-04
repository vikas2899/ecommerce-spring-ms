package com.ecommerce.order_service.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class AddCartRequestDTO {
    @NotNull(message = "Product ID cannot be null")
    private UUID productId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
