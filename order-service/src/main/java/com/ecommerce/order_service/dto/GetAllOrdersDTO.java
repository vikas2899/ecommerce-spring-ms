package com.ecommerce.order_service.dto;

import java.util.List;

public class GetAllOrdersDTO {
   private List<GetOrderResponseDTO> orders;

    public List<GetOrderResponseDTO> getOrders() {
        return orders;
    }

    public void setOrders(List<GetOrderResponseDTO> orders) {
        this.orders = orders;
    }
}
