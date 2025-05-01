package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.InventoryResponseDTO;
import com.ecommerce.inventory_service.exception.ProductNotFoundException;
import com.ecommerce.inventory_service.mapper.InventoryMapper;
import com.ecommerce.inventory_service.model.Inventory;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public InventoryResponseDTO getProductInventoryById(UUID id) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(id);
        if(inventory.isEmpty()) {
            throw new ProductNotFoundException("Product with given id not found in inventory: " + id);
        }

        return InventoryMapper.toDTO(inventory.get());
    }

}
