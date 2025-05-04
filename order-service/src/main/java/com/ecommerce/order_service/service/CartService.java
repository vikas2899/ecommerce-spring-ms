package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.AddCartRequestDTO;
import com.ecommerce.order_service.dto.AddCartResponseDTO;
import com.ecommerce.order_service.model.Cart;
import com.ecommerce.order_service.model.CartItem;
import com.ecommerce.order_service.repository.CartItemRepository;
import com.ecommerce.order_service.repository.CartRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import javax.smartcardio.CardException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;

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

}
