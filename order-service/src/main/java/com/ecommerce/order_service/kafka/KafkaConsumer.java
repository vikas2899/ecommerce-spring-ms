package com.ecommerce.order_service.kafka;

import com.ecommerce.order_service.dto.PaymentResultEventDTO;
import com.ecommerce.order_service.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final KafkaProducer kafkaProducer;
    private final OrderService orderService;

    public KafkaConsumer(KafkaProducer kafkaProducer, OrderService orderService) {
        this.kafkaProducer = kafkaProducer;
        this.orderService = orderService;
    }

    @KafkaListener(topics = "PAYMENT-RESULT", groupId = "order-service")
    public void consumeEvent(byte[] message) {
        try {
            PaymentResultEventDTO event = objectMapper.readValue(message, PaymentResultEventDTO.class);
            log.info("Received event (PAYMENT-RESULT): orderId={}, amount={}, status={}", event.getOrderId(), event.getTotalAmount(), event.getStatus());

            orderService.updateOrderStatus(event);

        } catch (Exception e) {
            log.error("Failed to deserialize PAYMENT-RESULT event", e);
        }
    }
}
