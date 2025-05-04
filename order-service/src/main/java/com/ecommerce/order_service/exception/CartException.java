package com.ecommerce.order_service.exception;

public class CartException extends RuntimeException {
    public CartException(String message) {
        super(message);
    }
}
