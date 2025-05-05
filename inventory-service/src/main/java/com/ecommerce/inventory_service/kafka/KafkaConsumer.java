package com.ecommerce.inventory_service.kafka;

import com.ecommerce.inventory_service.dto.InventoryEventDTO;
import com.ecommerce.inventory_service.service.InventoryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final InventoryService inventoryService;

    public KafkaConsumer(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @KafkaListener(topics = "INVENTORY-UPDATE", groupId = "inventory-service")
    public void consumeEvent(byte[] message) {
        try {
            List<InventoryEventDTO> eventList = objectMapper.readValue(
                    message,
                    new TypeReference<List<InventoryEventDTO>>() {}
            );
            for (InventoryEventDTO event : eventList) {
                log.info("Received inventory event: productId={}, quantity={}", event.getProductId(), event.getQuantity());
            }

            inventoryService.updateInventory(eventList);
        } catch (Exception e) {
            log.error("Failed to deserialize INVENTORY-UPDATE event", e);
        }
    }
}
