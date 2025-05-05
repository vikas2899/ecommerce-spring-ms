package com.ecommerce.payment_service.kafka;

import com.ecommerce.payment_service.dto.OrderPlacedEventDTO;
import com.ecommerce.payment_service.dto.PaymentResultEventDTO;
import com.ecommerce.payment_service.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final PaymentService paymentService;
    private final KafkaProducer kafkaProducer;

    public KafkaConsumer(PaymentService paymentService, KafkaProducer kafkaProducer) {
        this.paymentService = paymentService;
        this.kafkaProducer = kafkaProducer;
    }

    @KafkaListener(topics = "ORDER-PLACED", groupId = "payment-service")
    public void consumeEvent(byte[] message) {
        try {
            OrderPlacedEventDTO event = objectMapper.readValue(message, OrderPlacedEventDTO.class);
            log.info("Received event (ORDER-PLACED): orderId={}, amount={}", event.getOrderId(), event.getTotalAmount());

            boolean isPaymentSuccess = paymentService.mockPayment();
            PaymentResultEventDTO paymentResultEventDTO = new PaymentResultEventDTO();
            paymentResultEventDTO.setOrderId(event.getOrderId());
            paymentResultEventDTO.setTotalAmount(event.getTotalAmount());
            paymentResultEventDTO.setStatus(isPaymentSuccess ? "SUCCESS" : "FAILED");
            paymentResultEventDTO.setSuccess(isPaymentSuccess);
            paymentResultEventDTO.getUserId(event.getUserId());

            // Sending event for payment success / failure
            kafkaProducer.sendEvent("PAYMENT-RESULT", event.getOrderId(), paymentResultEventDTO);

        } catch (Exception e) {
            log.error("Failed to deserialize ORDER-PLACED event", e);
        }
    }
}

