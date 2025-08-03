package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.exception.CartException;
import com.ecommerce.order_service.exception.EmptyCartException;
import com.ecommerce.order_service.exception.ProductNotFoundException;
import com.ecommerce.order_service.exception.ProductOutOfStockException;
import com.ecommerce.order_service.kafka.KafkaProducer;
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
    private final KafkaProducer kafkaProducer;

    public OrderService(OrderRepository orderRepository, OrderItemsRepository orderItemsRepository,
                        CartItemRepository cartItemRepository, CartRepository cartRepository,
                        RestTemplate restTemplate, @Value("${product.service.url}") String productServiceUrl,
                        @Value("${inventory.service.url}") String inventoryServiceUrl,
                        KafkaProducer kafkaProducer) {
        this.orderRepository = orderRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.cartItemRepository = cartItemRepository;
        this.cartRepository = cartRepository;
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
        this.inventoryServiceUrl = inventoryServiceUrl;
        this.kafkaProducer = kafkaProducer;
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

    public GetAllOrdersDTO getOrderDetails(UUID userId, String token) throws Exception {
        Optional<List<Order>> order = orderRepository.findByUserId(userId);
        if(order.isEmpty()) {
            throw new Exception("Order not found");
        }

        GetAllOrdersDTO orders = new GetAllOrdersDTO();
        List<GetOrderResponseDTO> orderResponse = new ArrayList<>();

        List<Order> orderList = order.get();
        orderList.sort(Comparator.comparing(Order::getCreatedAt).reversed());

        orderList.forEach(order1 -> {
            List<OrderItem> orderItem = orderItemsRepository.findByOrderId(order1.getId());
            GetOrderResponseDTO response = new GetOrderResponseDTO();
            response.setId(order1.getId().toString());
            response.setOrderStatus(order1.getStatus());
            response.setCreatedAt(order1.getCreatedAt().toString());

            List<CartProductsDTO> product = new ArrayList<>();

            orderItem.forEach(o -> {
                ProductResponseDTO p = fetchProduct(o.getProductId(), token);
                CartProductsDTO orderProduct = new CartProductsDTO();

                orderProduct.setId(p.getId());
                orderProduct.setName(p.getName());
                orderProduct.setDescription(p.getDescription());
                orderProduct.setImages(p.getImages());
                orderProduct.setPrice(o.getPrice().floatValue());
                orderProduct.setQuantity(o.getQuantity());


                product.add(orderProduct);
            });

            response.setProducts(product);
            orderResponse.add(response);
        });

        orders.setOrders(orderResponse);

        return orders;
    }

    @Transactional
    public OrderResponseDTO createOrder(OrderRequestDTO orderRequestDTO, UUID userId, String token) throws Exception {
        UUID cartId = orderRequestDTO.getCartId();

        List<CartItem> cartItems = getUserCartProducts(cartId, userId, token);
        if(cartItems.isEmpty()) {
            throw new EmptyCartException("Cart is empty for userId: " + userId);
        }

        double totalAmount = cartItems.stream().mapToDouble(item ->
                item.getQuantity() * fetchProduct(item.getProductId(), token).getPrice()).sum();

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
            orderItem.setOrderedAt(LocalDateTime.now());

            orderItem.setPrice(BigDecimal.valueOf(fetchProduct(item.getProductId(), token).getPrice()));
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
        orderResponseDTO.setUserId(userId.toString());

        log.info("User Order placed: {}", savedOrder.getId());

        // send event for notification and payment service
        kafkaProducer.sendEvent("ORDER-PLACED", savedOrder.getId().toString(), orderResponseDTO);
        return orderResponseDTO;
    }

    private ProductResponseDTO fetchProduct(UUID productId, String authHeader) {
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
                return product;
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

    public void updateOrderStatus(PaymentResultEventDTO event) throws Exception {
        UUID orderId = event.getOrderId();
        String status = Objects.equals(event.getStatus(), "SUCCESS") ? "CONFIRMED" : "CANCELLED";

        Optional<Order> order = orderRepository.findById(orderId);
        if(order.isEmpty()) {
            throw new Exception("Order not found with id: " + orderId);
        }

        if(event.getStatus().equals("SUCCESS")) {
            // deduct stock from inventory is status is SUCCESS
            List<InventoryEventDTO> inventoryEventDTOS = new ArrayList<>();
            List<OrderItem> orderItems = orderItemsRepository.findByOrderId(orderId);

            for(OrderItem item: orderItems) {
                log.info("ID: {}, Quantity: {}", item.getId(), item.getQuantity());
                InventoryEventDTO eventDTO = new InventoryEventDTO();
                eventDTO.setProductId(item.getProductId());
                eventDTO.setQuantity(item.getQuantity());

                inventoryEventDTOS.add(eventDTO);
            }
            // Send event to inventory and notification
            kafkaProducer.sendEvent("INVENTORY-UPDATE", orderId.toString(), inventoryEventDTOS);
            kafkaProducer.sendEvent("ORDER-CONFIRMED", orderId.toString(), null);
        } else {
             kafkaProducer.sendEvent("ORDER-CANCELLED", orderId.toString(), null);
        }

        order.get().setStatus(status);
        orderRepository.save(order.get());
    }


}
