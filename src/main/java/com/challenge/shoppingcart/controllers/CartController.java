package com.challenge.shoppingcart.controllers;

import com.challenge.shoppingcart.dtos.*;
import com.challenge.shoppingcart.services.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/{cartId}/items")
    public ResponseEntity<Void> removeItem(
            @PathVariable Long cartId,
            @Valid @RequestBody RemoveItemRequest request) {
        cartService.removeItem(cartId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{cartId}/items")
    public ResponseEntity<List<CartItemDto>> getCartItems(@PathVariable Long cartId,
                                                          @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(cartService.getCartItems(cartId, authHeader));
    }
}