package com.ecommerce.inventory_service.mapper;

import com.ecommerce.inventory_service.dto.InventoryResponseDTO;
import com.ecommerce.inventory_service.model.Inventory;

public class InventoryMapper {
    public static InventoryResponseDTO toDTO(Inventory inventory) {
        InventoryResponseDTO responseDTO = new InventoryResponseDTO();

        responseDTO.setId(inventory.getId().toString());
        responseDTO.setProductId(inventory.getProductId().toString());
        responseDTO.setQuantity(inventory.getQuantity());
        responseDTO.setUpdatedAt(inventory.getUpdatedAt().toString());

        return responseDTO;
    }
}
