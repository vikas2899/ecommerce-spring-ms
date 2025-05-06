package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.InventoryEventDTO;
import com.ecommerce.inventory_service.dto.InventoryResponseDTO;
import com.ecommerce.inventory_service.exception.ProductNotFoundException;
import com.ecommerce.inventory_service.kafka.KafkaProducer;
import com.ecommerce.inventory_service.mapper.InventoryMapper;
import com.ecommerce.inventory_service.model.Inventory;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaProducer kafkaProducer;

    public InventoryService(InventoryRepository inventoryRepository, KafkaProducer kafkaProducer) {
        this.inventoryRepository = inventoryRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public InventoryResponseDTO getProductInventoryById(UUID id) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(id);
        if(inventory.isEmpty()) {
            throw new ProductNotFoundException("Product with given id not found in inventory: " + id);
        }

        return InventoryMapper.toDTO(inventory.get());
    }

    public boolean isStockAvailableForProductId(UUID productId, int askedQuantity) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
        if(inventory.isEmpty()) {
            throw new ProductNotFoundException("Product with given id not found in inventory: " + productId);
        }

        return inventory.get().getQuantity() >= askedQuantity;
    }

    public void updateInventory(List<InventoryEventDTO> inventoryEventDTO) throws JsonProcessingException {
        for (InventoryEventDTO item : inventoryEventDTO) {
            if(isStockAvailableForProductId(item.getProductId(), item.getQuantity())) {
                Optional<Inventory> inventory = inventoryRepository.findByProductId(item.getProductId());
                if(inventory.isPresent()) {
                    int updatedQuantity = inventory.get().getQuantity() - item.getQuantity();
                    inventory.get().setQuantity(updatedQuantity);

                    Inventory updatedInventory = inventoryRepository.save(inventory.get());
                    kafkaProducer.sendEvent("ORDER-INVENTORY-UPDATED", item.getProductId().toString(), updatedInventory);
                }
            } else {
                throw new ProductNotFoundException("Product with given id not found in inventory: " + item.getProductId());
            }
        }
    }

}
