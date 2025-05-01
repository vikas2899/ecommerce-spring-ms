package com.ecommerce.inventory_service.controller;

import com.ecommerce.inventory_service.dto.InventoryResponseDTO;
import com.ecommerce.inventory_service.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/stock")
    public ResponseEntity<Map<UUID, InventoryResponseDTO>> getStockForProducts(@RequestBody List<UUID> productIds) {
        Map<UUID, InventoryResponseDTO> stocks = new HashMap<>();
        for(UUID productId: productIds) {
            stocks.put(productId, inventoryService.getProductInventoryById(productId));
        }

        return ResponseEntity.ok().body(stocks);
    }

}
