package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.InventoryCheckRequestDTO;
import com.ecommerce.order_service.dto.OrderRequestDTO;
import com.ecommerce.order_service.dto.OrderResponseDTO;
import com.ecommerce.order_service.dto.ProductResponseDTO;
import com.ecommerce.order_service.exception.CartException;
import com.ecommerce.order_service.exception.EmptyCartException;
import com.ecommerce.order_service.exception.ProductNotFoundException;
import com.ecommerce.order_service.exception.ProductOutOfStockException;
import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.model.Order;
import com.ecommerce.order_service.model.OrderItem;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.CartRepository;
import com.ecommerce.order_service.repository.OrderItemsRepository;
import com.ecommerce.order_service.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final RestTemplate restTemplate;
    private final String productServiceUrl;
    private final String inventoryServiceUrl;

    public OrderService(OrderRepository orderRepository, OrderItemsRepository orderItemsRepository,
                        CartItemRepository cartItemRepository, CartRepository cartRepository,
                        RestTemplate restTemplate, @Value("${product.service.url}") String productServiceUrl,
                        @Value("${inventory.service.url}") String inventoryServiceUrl) {
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    public List<CartItem> getUserCartProducts(UUID cartId, UUID userId, String token) throws Exception {
        Optional<Cart> cart = cartRepository.findById(cartId);
        if(cart.isEmpty()) {
            throw new CartException("Cart not found");
        }
        if(!cart.get().getUserId().equals(userId)) {
            throw new CartException("Cart does not belong to user");
        }

        List<CartItem> cartItems = cartItemRepository.findByCartId(cartId);
        try {
            checkIfCartIsValid(cartItems, token);
        } catch (ProductOutOfStockException e) {
            throw new ProductOutOfStockException("Product out of stock");
        } catch (Exception e) {
            throw new Exception("Something went wrong while checking inventory");
        }

        return cartItems;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, UUID userId, String token) throws Exception {
        UUID cartId = orderRequestDTO.getCartId();

        List<CartItem> cartItems = getUserCartProducts(cartId, userId, token);
        if(cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty for userId: " + userId);
        }

        double totalAmount = cartItems.stream().mapToDouble(item ->
                item.getQuantity() * fetchProductPrice(item.getProductId(), token)).sum();

        Order order = new Order();
        order.setUserId(userId);
        order.setStatus("PLACED");
        order.setTotalAmount(BigDecimal.valueOf(totalAmount));
        order.setCreatedAt(LocalDateTime.now());
        Order savedOrder = orderRepository.save(order);

        for(CartItem item: cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(item.getProductId());
            orderItem.setQuantity(item.getQuantity());

            orderItem.setPrice(BigDecimal.valueOf(fetchProductPrice(item.getProductId(), token)));
            orderItemsRepository.save(orderItem);
        }

        cartItemRepository.deleteByCart_Id(cartId);
        OrderResponseDTO orderResponseDTO = new OrderResponseDTO();
        orderResponseDTO.setSuccess(true);
        orderResponseDTO.setMessage("Order Placed");
        orderResponseDTO.setOrderId(savedOrder.getId().toString());
        orderResponseDTO.setStatus("PLACED");
        orderResponseDTO.setTotalAmount(savedOrder.getTotalAmount().doubleValue());
        orderResponseDTO.setCreatedAt(savedOrder.getCreatedAt().toString());
        orderResponseDTO.setPaymentStatus("PENDING");

        log.info("User Order placed: {}", savedOrder.getId());

        return orderResponseDTO;
    }

    private double fetchProductPrice(UUID productId, String authHeader) {
        String url = productServiceUrl + "/" + productId;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<ProductResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    ProductResponseDTO.class
            );

            ProductResponseDTO product = response.getBody();

            if (product != null) {
                return product.getPrice();
            } else {
                throw new RuntimeException("Failed to fetch product or product not found");
            }
        } catch (HttpClientErrorException e) {
            throw new ProductNotFoundException("Client error while fetching product: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
        } catch (HttpServerErrorException e) {
            throw new RuntimeException("Server error from product service: " + e.getStatusCode(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Something went wrong while calling product service: " + e.getMessage(), e);
        }

    }

    private void checkIfCartIsValid(List<CartItem> cartItems, String authHeader) throws Exception {
        String url = inventoryServiceUrl + "/check";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authHeader);

        for (CartItem item : cartItems) {
            InventoryCheckRequestDTO requestBody = new InventoryCheckRequestDTO();
            requestBody.setProductId(item.getProductId());
            requestBody.setQuantity(item.getQuantity());
            HttpEntity<InventoryCheckRequestDTO> requestEntity = new HttpEntity<>(requestBody, headers);

            try {
                ResponseEntity<Void> response = restTemplate.postForEntity(url, requestEntity, Void.class);
                if (response.getStatusCode() != HttpStatus.OK) {
                    throw new Exception("Inventory check failed for productId: " + item.getProductId());
                }
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                    throw new ProductOutOfStockException("Product out of stock: productId = " + item.getProductId());
                } else {
                    throw new Exception("Error calling inventory service: " + e.getMessage(), e);
                }
            }
        }
    }


}
