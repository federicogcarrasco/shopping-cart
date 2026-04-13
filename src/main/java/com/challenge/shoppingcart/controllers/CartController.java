package com.challenge.shoppingcart.controllers;

import com.challenge.shoppingcart.dtos.CartDto;
import com.challenge.shoppingcart.dtos.CreateCartRequest;
import com.challenge.shoppingcart.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;
import com.challenge.shoppingcart.dtos.AddItemRequest;
import com.challenge.shoppingcart.dtos.CartItemDto;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartDto> createCart(
            @Valid @RequestBody CreateCartRequest request,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.createCart(request, authHeader));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CartDto>> getCartsByUserId(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(cartService.getCartsByUserId(userId, authHeader));
    }

    @PostMapping("/{cartId}/items")
    public ResponseEntity<CartItemDto> addItem(
            @PathVariable Long cartId,
            @Valid @RequestBody AddItemRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(cartService.addItem(cartId, request));
    }
}