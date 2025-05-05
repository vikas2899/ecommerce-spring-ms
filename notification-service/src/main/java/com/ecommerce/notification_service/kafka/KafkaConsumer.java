package com.ecommerce.notification_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "ORDER-PLACED", groupId = "notification-service")
    public void consumeEvent(byte[] message) {
        System.out.println("Received event: (ORDER-PLACED) " + new String(message));
    }
}
