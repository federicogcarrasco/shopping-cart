package com.challenge.shoppingcart.services;

import com.challenge.shoppingcart.dtos.*;
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
import com.challenge.shoppingcart.entities.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.challenge.shoppingcart.entities.CartItem;
import com.challenge.shoppingcart.entities.Product;
import com.challenge.shoppingcart.exceptions.InvalidOperationException;
import com.challenge.shoppingcart.repositories.CartItemRepository;
import com.challenge.shoppingcart.repositories.ProductRepository;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final JwtService jwtService;
    private final OrderProcessingService orderProcessingService;
    private final MeterRegistry meterRegistry;

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
        meterRegistry.counter("carts.created").increment();

        return CartDto.builder()
                .id(saved.getId())
                .username(user.getUsername())
                .status(saved.getStatus())
                .build();
    }

    public List<CartDto> getCartsByUserId(Long userId, String authHeader) {
        String token = authHeader.substring(7);
        Long tokenUserId = jwtService.extractUserId(token);
        String tokenRole = jwtService.extractRole(token);

        if (!tokenRole.equals(UserRole.ADMIN.name()) && !tokenUserId.equals(userId)) {
            throw new UnauthorizedOperationException(
                    "No podés ver los carritos de otro usuario");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado con id: " + userId));

        List<Cart> carts = cartRepository.findByUser(user);

        if (carts.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No se encontraron carritos para el usuario: " + user.getUsername());
        }

        return carts.stream()
                    .map(cart -> CartDto.builder()
                        .id(cart.getId())
                        .username(user.getUsername())
                        .status(cart.getStatus())
                        .build())
                    .collect(Collectors.toList());
    }

    @Transactional
    public CartItemDto addItem(Long cartId, AddItemRequest request) {
        Cart cart = cartRepository.findByIdWithLock(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrito no encontrado con id: " + cartId));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Solo se pueden agregar items a carritos con estado ACTIVE");
        }

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Producto no encontrado con id: " + request.getProductId()));

        Optional<CartItem> optionalItem = cartItemRepository.findByCartIdAndProductId(cartId, product.getId());

        CartItem item;
        if (optionalItem.isPresent()) {
            item = optionalItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            item = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
        }

        CartItem saved = cartItemRepository.save(item);
        log.info("Producto {} agregado al carrito {}", product.getId(), cartId);
        meterRegistry.counter("carts.items.added").increment();

        return CartItemDto.builder()
                .productDto(
                        ProductDto.builder()
                                .name(product.getName())
                                .price(product.getPrice())
                                .category(ProductCategoryDto
                                        .builder()
                                        .name(product.getCategory().getName())
                                        .discount(product.getCategory().getDiscount())
                                        .build())
                                .build())
                .quantity(saved.getQuantity())
                .build();
    }

    @Transactional
    public void removeItem(Long cartId, RemoveItemRequest request) {
        Cart cart = cartRepository.findByIdWithLock(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrito no encontrado con id: " + cartId));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Solo se pueden eliminar items de carritos con estado ACTIVE");
        }

        CartItem item = cartItemRepository.findByCartIdAndProductId(cartId, request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto con id " + request.getProductId() + " no está en el carrito"));

        cartItemRepository.delete(item);
        log.info("Producto {} eliminado del carrito {}", request.getProductId(), cartId);
        meterRegistry.counter("carts.items.removed").increment();
    }

    public List<CartItemDto> getCartItems(Long cartId, String authHeader) {
        String token = authHeader.substring(7);
        Long tokenUserId = jwtService.extractUserId(token);
        String tokenRole = jwtService.extractRole(token);

        Cart cart = cartRepository.findById(cartId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                            "Carrito no encontrado con id: " + cartId));

        if (!tokenRole.equals(UserRole.ADMIN.name()) && !tokenUserId.equals(cart.getUser().getId())) {
            throw new UnauthorizedOperationException(
                    "No podés ver los carritos de otro usuario");
        }

        List<CartItem> items = cartItemRepository.findByCartId(cartId);

        if (items.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No se encontraron productos en el carrito con id: " + cartId);
        }

        return items.stream()
                .map(item -> CartItemDto.builder()
                        .quantity(item.getQuantity())
                        .productDto(ProductDto.builder()
                                .name(item.getProduct().getName())
                                .price(item.getProduct().getPrice())
                                .category(ProductCategoryDto.builder()
                                        .name(item.getProduct().getCategory().getName())
                                        .discount(item.getProduct().getCategory().getDiscount())
                                        .build())
                                .build())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public String processCart(Long cartId) {
        Cart cart = cartRepository.findByIdWithLock(cartId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrito no encontrado con id: " + cartId));

        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new InvalidOperationException(
                    "Solo se pueden procesar carritos con estado ACTIVE");
        }

        cart.setStatus(CartStatus.PROCESSING);
        cartRepository.save(cart);
        meterRegistry.counter("carts.orders.processing").increment();

        orderProcessingService.process(cartId);

        return "Estamos procesando su orden";
    }
}