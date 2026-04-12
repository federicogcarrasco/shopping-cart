package com.challenge.shoppingcart.services;

import com.challenge.shoppingcart.dtos.CartDto;
import com.challenge.shoppingcart.dtos.CreateCartRequest;
import com.challenge.shoppingcart.entities.Cart;
import com.challenge.shoppingcart.entities.CartStatus;
import com.challenge.shoppingcart.entities.User;
import com.challenge.shoppingcart.exceptions.ResourceNotFoundException;
import com.challenge.shoppingcart.exceptions.UnauthorizedOperationException;
import com.challenge.shoppingcart.repositories.CartRepository;
import com.challenge.shoppingcart.repositories.UserRepository;
import com.challenge.shoppingcart.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public CartDto createCart(CreateCartRequest request, String authHeader) {
        Long tokenUserId = jwtService.extractUserId(authHeader.substring(7));

        if (!tokenUserId.equals(request.getUserId())) {
            throw new UnauthorizedOperationException(
                    "No podés crear un carrito para otro usuario");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + request.getUserId()));

        Cart cart = Cart.builder()
                .user(user)
                .status(CartStatus.ACTIVE)
                .build();

        Cart saved = cartRepository.save(cart);
        log.info("Carrito creado con id {} para usuario {}", saved.getId(), user.getUsername());

        return CartDto.builder()
                .id(saved.getId())
                .username(user.getUsername())
                .status(saved.getStatus())
                .build();
    }
}