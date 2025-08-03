package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.smartcardio.CardException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final RestTemplate restTemplate;
    private final String productServiceUrl;


    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       RestTemplate restTemplate, @Value("${product.service.url}") String productServiceUrl) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.restTemplate = restTemplate;
        this.productServiceUrl = productServiceUrl;
    }

    @Transactional
    private Cart createCart(UUID userId) {
        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setCreatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    @Transactional
    public AddCartResponseDTO addProductsToCart(List<AddCartRequestDTO> addCartRequestDTO, UUID userId) throws CardException {
        if(addCartRequestDTO.isEmpty()) {
            throw new CardException("Cart items cannot be empty");
        }

        Optional<Cart> cart = cartRepository.findByUserId(userId);
        Cart userCart = cart.orElseGet(() -> createCart(userId));

        addCartRequestDTO.stream().map(item -> {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(item.getProductId());
            cartItem.setQuantity(item.getQuantity());
            cartItem.setCart(userCart);
            return cartItem;
        }).forEach(cartItemRepository::save);

        AddCartResponseDTO cartResponseDTO = new AddCartResponseDTO();
        cartResponseDTO.setId(userCart.getId().toString());
        cartResponseDTO.setItems(addCartRequestDTO);

        return cartResponseDTO;

    }


    public GetCartResponseDTO getCartItems(UUID userId)  {
        Optional<Cart> cart = cartRepository.findByUserId(userId);
        Cart userCart = cart.orElseGet(() -> createCart(userId));

        List<CartItem> userCartItems = cartItemRepository.findByCartId(userCart.getId());
        GetCartResponseDTO responseDTO = new GetCartResponseDTO();
        responseDTO.setId(userCart.getId().toString());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<CartProductsDTO> productDetail = userCartItems.stream().map(item -> {
            CartProductsDTO itemDto = new CartProductsDTO();

            ResponseEntity<ProductResponseDTO> response = restTemplate.exchange(
                    productServiceUrl + "/" + item.getProductId(),
                    HttpMethod.GET,
                    null,
                    ProductResponseDTO.class
            );

            itemDto.setId(item.getProductId().toString());
            itemDto.setQuantity(item.getQuantity());
            itemDto.setName(response.getBody().getName());
            itemDto.setPrice(response.getBody().getPrice());
            itemDto.setDescription(response.getBody().getDescription());
            itemDto.setCategoryName(response.getBody().getCategoryName());
            itemDto.setImages(response.getBody().getImages());
            itemDto.setStockStatus(response.getBody().getStockStatus());

            return itemDto;
        }).toList();

        responseDTO.setProducts(productDetail);

        return responseDTO;
    }

    @Transactional
    public void updateCart(UpdateCartRequestDTO updateCartRequestDTO, UUID userId) {
        Optional<Cart> userCart = cartRepository.findByUserId(userId);

        if(userCart.isEmpty()) {
            return;
        }

        Cart cart = userCart.get();
        CartItem itemToUpdate = cartItemRepository.findByCartId(cart.getId())
                .stream()
                .filter(item -> item.getProductId().equals(updateCartRequestDTO.getProductId()))
                .findFirst()
                .orElse(null);

        if (itemToUpdate != null) {
            itemToUpdate.setQuantity(updateCartRequestDTO.getQuantity());
            cartItemRepository.save(itemToUpdate);
        }
    }

    @Transactional
    public void removeCartItem(UUID productId, UUID userId) {
        Optional<Cart> userCart = cartRepository.findByUserId(userId);

        if(userCart.isEmpty()) {
            return;
        }
        Cart cart = userCart.get();
        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
    }
}
