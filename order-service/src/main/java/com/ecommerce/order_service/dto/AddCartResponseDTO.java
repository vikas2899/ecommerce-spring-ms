package com.ecommerce.order_service.dto;

import java.util.List;

public class AddCartResponseDTO {
    private String id;
    private List<AddCartRequestDTO> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<AddCartRequestDTO> getItems() {
        return items;
    }

    public void setItems(List<AddCartRequestDTO> items) {
        this.items = items;
    }
}
