package com.ecommerce.order_service.repository;

import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<List<Order>> findByUserId(UUID id);
}
