package com.ecommerce.notification_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumer {

    @KafkaListener(topics = "ORDER-PLACED", groupId = "notification-service")
    public void consumeEvent(byte[] message) {
        System.out.println("Received event: (ORDER-PLACED) " + new String(message));
    }

    @KafkaListener(topics = "ORDER-INVENTORY-UPDATED", groupId = "notification-service")
    public void consumeEventForInventory(byte[] message) {
        System.out.println("Received event: (ORDER-INVENTORY-UPDATED) " + new String(message));
    }

    @KafkaListener(topics = "ORDER-CONFIRMED", groupId = "notification-service")
    public void consumeEventForOrderConfirmed(byte[] message) {
        System.out.println("Received event: (ORDER-CONFIRMED) " + new String(message));
    }

    @KafkaListener(topics = "ORDER-CANCELLED", groupId = "notification-service")
    public void consumeEventForOrderCancelled(byte[] message) {
        System.out.println("Received event: (ORDER-CANCELLED) " + new String(message));
    }
}
