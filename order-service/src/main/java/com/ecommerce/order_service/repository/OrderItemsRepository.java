package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItem, UUID> {
}
