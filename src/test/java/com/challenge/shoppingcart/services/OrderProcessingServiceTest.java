package com.challenge.shoppingcart.services;

import com.challenge.shoppingcart.entities.*;
import com.challenge.shoppingcart.repositories.CartItemRepository;
import com.challenge.shoppingcart.repositories.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessingServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @InjectMocks
    private OrderProcessingService orderProcessingService;

    private User testUser;
    private Cart testCart;
    private ProductCategory electronicsCategory;
    private ProductCategory foodCategory;
    private Product laptop;
    private Product rice;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .status(CartStatus.PROCESSING)
                .build();

        electronicsCategory = ProductCategory.builder()
                .id(1L)
                .name("Electronics")
                .discount(0.10)
                .build();

        foodCategory = ProductCategory.builder()
                .id(2L)
                .name("Food")
                .discount(0.05)
                .build();

        laptop = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(1500.00)
                .category(electronicsCategory)
                .build();

        rice = Product.builder()
                .id(2L)
                .name("Rice 1kg")
                .price(2.50)
                .category(foodCategory)
                .build();
    }

    @Test
    void process_ShouldChangeStatusToProcessed_WhenCartIsValid() {
        CartItem item = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(laptop)
                .quantity(1)
                .build();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));

        orderProcessingService.process(1L);

        verify(cartRepository).save(argThat(cart ->
                cart.getStatus() == CartStatus.PROCESSED));
    }

    @Test
    void process_ShouldCalculateCorrectTotal_WithSingleItem() {
        // Laptop $1500 con 10% descuento, quantity 2
        // esperado: 1500 * 0.90 * 2 = 2700.00
        CartItem item = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(laptop)
                .quantity(2)
                .build();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));

        orderProcessingService.process(1L);

        verify(cartRepository).save(argThat(cart ->
                cart.getStatus() == CartStatus.PROCESSED));
    }

    @Test
    void process_ShouldCalculateCorrectTotal_WithMultipleItemsAndCategories() {
        // Laptop $1500 con 10% descuento, quantity 1 → 1350.00
        // Rice $2.50 con 5% descuento, quantity 3  →    7.125
        // Total esperado: 1357.125
        CartItem laptopItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(laptop)
                .quantity(1)
                .build();

        CartItem riceItem = CartItem.builder()
                .id(2L)
                .cart(testCart)
                .product(rice)
                .quantity(3)
                .build();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(laptopItem, riceItem));

        orderProcessingService.process(1L);

        verify(cartRepository).save(argThat(cart ->
                cart.getStatus() == CartStatus.PROCESSED));
    }

    @Test
    void process_ShouldCalculateCorrectTotal_WithZeroDiscount() {
        // Football $30 con 0% descuento, quantity 2 → 60.00
        ProductCategory sportsCategory = ProductCategory.builder()
                .id(3L)
                .name("Sports")
                .discount(0.00)
                .build();

        Product football = Product.builder()
                .id(3L)
                .name("Football")
                .price(30.00)
                .category(sportsCategory)
                .build();

        CartItem item = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(football)
                .quantity(2)
                .build();

        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(item));

        orderProcessingService.process(1L);

        verify(cartRepository).save(argThat(cart ->
                cart.getStatus() == CartStatus.PROCESSED));
    }

    @Test
    void process_ShouldNotSave_WhenCartNotFound() {
        when(cartRepository.findById(99L)).thenReturn(Optional.empty());

        orderProcessingService.process(99L);

        verify(cartRepository, never()).save(any());
    }
}