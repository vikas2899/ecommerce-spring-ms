package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.dto.AddCartRequestDTO;
import com.ecommerce.order_service.dto.AddCartResponseDTO;
import com.ecommerce.order_service.exception.CartException;
import com.ecommerce.order_service.service.CartService;
import com.ecommerce.order_service.utils.JwtUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cart")
public class CartController {

    private static final Logger log = LoggerFactory.getLogger(CartController.class);
    private final CartService cartService;
    private final JwtUtils jwtUtils;

    public CartController(CartService cartService, JwtUtils jwtUtils) {
        this.cartService = cartService;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/add")
    public ResponseEntity<AddCartResponseDTO> addItemsToCart(@Valid @RequestBody List<AddCartRequestDTO> addCartRequestDTO, @RequestHeader("Authorization") String authHeader) {
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            UUID userId = jwtUtils.getId(authHeader.replace("Bearer ", ""));
            AddCartResponseDTO cartResponseDTO = cartService.addProductsToCart(addCartRequestDTO, userId);
            log.info("Items added to user's cart.");
            return ResponseEntity.ok().body(cartResponseDTO);
        } catch (CartException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }

    }

}
