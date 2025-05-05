package com.ecommerce.order_service.dto;

import java.util.UUID;

public class OrderRequestDTO {
    private UUID cartId;
    private String paymentMethod;

    public UUID getCartId() {
        return cartId;
    }

    public void setCartId(UUID cartId) {
        this.cartId = cartId;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
