package com.ecommerce.inventory_service.service;

import com.ecommerce.inventory_service.dto.InventoryEventDTO;
import com.ecommerce.inventory_service.dto.InventoryResponseDTO;
import com.ecommerce.inventory_service.exception.ProductNotFoundException;
import com.ecommerce.inventory_service.mapper.InventoryMapper;
import com.ecommerce.inventory_service.model.Inventory;
import com.ecommerce.inventory_service.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public boolean isStockAvailableForProductId(UUID productId, int askedQuantity) {
        Optional<Inventory> inventory = inventoryRepository.findByProductId(productId);
        if(inventory.isEmpty()) {
            throw new ProductNotFoundException("Product with given id not found in inventory: " + productId);
        }

        return inventory.get().getQuantity() >= askedQuantity;
    }

    public void updateInventory(List<InventoryEventDTO> inventoryEventDTO) {
        for (InventoryEventDTO item : inventoryEventDTO) {
            if(isStockAvailableForProductId(item.getProductId(), item.getQuantity())) {
                Optional<Inventory> inventory = inventoryRepository.findByProductId(item.getProductId());
                if(inventory.isPresent()) {
                    int updatedQuantity = inventory.get().getQuantity() - item.getQuantity();
                    inventory.get().setQuantity(updatedQuantity);

                    inventoryRepository.save(inventory.get());
                }
            } else {
                throw new ProductNotFoundException("Product with given id not found in inventory: " + item.getProductId());
            }
        }
    }

}
