package com.ecommerce.payment_service.service;

import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    public boolean mockPayment() {
        return Math.random() > 0.4;
    }

}
