package com.challenge.shoppingcart.services;

import com.challenge.shoppingcart.dtos.AddItemRequest;
import com.challenge.shoppingcart.dtos.CartDto;
import com.challenge.shoppingcart.dtos.CartItemDto;
import com.challenge.shoppingcart.dtos.CreateCartRequest;
import com.challenge.shoppingcart.dtos.RemoveItemRequest;
import com.challenge.shoppingcart.entities.*;
import com.challenge.shoppingcart.exceptions.InvalidOperationException;
import com.challenge.shoppingcart.exceptions.ResourceNotFoundException;
import com.challenge.shoppingcart.exceptions.UnauthorizedOperationException;
import com.challenge.shoppingcart.repositories.CartItemRepository;
import com.challenge.shoppingcart.repositories.CartRepository;
import com.challenge.shoppingcart.repositories.ProductRepository;
import com.challenge.shoppingcart.repositories.UserRepository;
import com.challenge.shoppingcart.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private OrderProcessingService orderProcessingService;

    @InjectMocks
    private CartService cartService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private ProductCategory testCategory;
    private CartItem testCartItem;
    private static final String AUTH_HEADER = "Bearer mockedToken";
    private static final String TOKEN = "mockedToken";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        testCategory = ProductCategory.builder()
                .id(1L)
                .name("Electronics")
                .discount(0.10)
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(1500.00)
                .category(testCategory)
                .build();

        testCart = Cart.builder()
                .id(1L)
                .user(testUser)
                .status(CartStatus.ACTIVE)
                .build();

        testCartItem = CartItem.builder()
                .id(1L)
                .cart(testCart)
                .product(testProduct)
                .quantity(1)
                .build();
    }

    // ===== createCart =====

    @Test
    void createCart_ShouldReturnCartDto_WhenUserIsOwner() {
        CreateCartRequest request = new CreateCartRequest();
        request.setUserId(1L);

        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartDto result = cartService.createCart(request, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals(CartStatus.ACTIVE, result.getStatus());
    }

    @Test
    void createCart_ShouldThrowException_WhenUserIsNotOwner() {
        CreateCartRequest request = new CreateCartRequest();
        request.setUserId(2L);

        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);

        assertThrows(UnauthorizedOperationException.class,
                () -> cartService.createCart(request, AUTH_HEADER));

        verify(cartRepository, never()).save(any());
    }

    @Test
    void createCart_ShouldThrowException_WhenUserNotFound() {
        CreateCartRequest request = new CreateCartRequest();
        request.setUserId(1L);

        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.createCart(request, AUTH_HEADER));
    }

    // ===== getCartsByUserId =====

    @Test
    void getCartsByUserId_ShouldReturnCarts_WhenUserIsOwner() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(List.of(testCart));

        List<CartDto> result = cartService.getCartsByUserId(1L, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getCartsByUserId_ShouldReturnCarts_WhenUserIsAdmin() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(2L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.ADMIN.name());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(List.of(testCart));

        List<CartDto> result = cartService.getCartsByUserId(1L, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCartsByUserId_ShouldThrowException_WhenUserIsNotOwnerAndNotAdmin() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(2L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());

        assertThrows(UnauthorizedOperationException.class,
                () -> cartService.getCartsByUserId(1L, AUTH_HEADER));
    }

    @Test
    void getCartsByUserId_ShouldThrowException_WhenCartsAreEmpty() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.findByUser(testUser)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.getCartsByUserId(1L, AUTH_HEADER));
    }

    // ===== addItem =====

    @Test
    void addItem_ShouldCreateNewCartItem_WhenProductNotInCart() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L)).thenReturn(Optional.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(
                CartItem.builder()
                        .id(1L)
                        .cart(testCart)
                        .product(testProduct)
                        .quantity(2)
                        .build());

        CartItemDto result = cartService.addItem(1L, request);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        assertEquals("Laptop", result.getProductDto().getName());
    }

    @Test
    void addItem_ShouldUpdateQuantity_WhenProductAlreadyInCart() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(
                CartItem.builder()
                        .id(1L)
                        .cart(testCart)
                        .product(testProduct)
                        .quantity(3)
                        .build());

        CartItemDto result = cartService.addItem(1L, request);

        assertEquals(3, result.getQuantity());
    }

    @Test
    void addItem_ShouldThrowException_WhenCartNotActive() {
        testCart.setStatus(CartStatus.PROCESSED);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(1);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));

        assertThrows(InvalidOperationException.class,
                () -> cartService.addItem(1L, request));

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void addItem_ShouldThrowException_WhenProductNotFound() {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(99L);
        request.setQuantity(1);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.addItem(1L, request));
    }

    // ===== removeItem =====

    @Test
    void removeItem_ShouldDeleteCartItem_WhenItemExists() {
        RemoveItemRequest request = new RemoveItemRequest();
        request.setProductId(1L);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 1L))
                .thenReturn(Optional.of(testCartItem));

        assertDoesNotThrow(() -> cartService.removeItem(1L, request));
        verify(cartItemRepository).delete(testCartItem);
    }

    @Test
    void removeItem_ShouldThrowException_WhenCartNotActive() {
        testCart.setStatus(CartStatus.PROCESSING);

        RemoveItemRequest request = new RemoveItemRequest();
        request.setProductId(1L);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));

        assertThrows(InvalidOperationException.class,
                () -> cartService.removeItem(1L, request));

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void removeItem_ShouldThrowException_WhenItemNotFound() {
        RemoveItemRequest request = new RemoveItemRequest();
        request.setProductId(99L);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 99L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.removeItem(1L, request));
    }

    // ===== getCartItems =====

    @Test
    void getCartItems_ShouldReturnItems_WhenUserIsOwner() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(testCartItem));

        List<CartItemDto> result = cartService.getCartItems(1L, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getProductDto().getName());
        assertEquals(1500.00, result.get(0).getProductDto().getPrice());
        assertEquals("Electronics", result.get(0).getProductDto().getCategory().getName());
        assertEquals(0.10, result.get(0).getProductDto().getCategory().getDiscount());
    }

    @Test
    void getCartItems_ShouldReturnItems_WhenUserIsAdmin() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(2L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.ADMIN.name());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of(testCartItem));

        List<CartItemDto> result = cartService.getCartItems(1L, AUTH_HEADER);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCartItems_ShouldThrowException_WhenUserIsNotOwnerAndNotAdmin() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(2L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));

        assertThrows(UnauthorizedOperationException.class,
                () -> cartService.getCartItems(1L, AUTH_HEADER));
    }

    @Test
    void getCartItems_ShouldThrowException_WhenCartNotFound() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());
        when(cartRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.getCartItems(1L, AUTH_HEADER));
    }

    @Test
    void getCartItems_ShouldThrowException_WhenCartIsEmpty() {
        when(jwtService.extractUserId(TOKEN)).thenReturn(1L);
        when(jwtService.extractRole(TOKEN)).thenReturn(UserRole.USER.name());
        when(cartRepository.findById(1L)).thenReturn(Optional.of(testCart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.getCartItems(1L, AUTH_HEADER));
    }

    // ===== processCart =====

    @Test
    void processCart_ShouldChangeStatusToProcessing_WhenCartIsActive() {
        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        String result = cartService.processCart(1L);

        assertEquals("Estamos procesando su orden", result);
        verify(cartRepository).save(any(Cart.class));
        verify(orderProcessingService).process(1L);
    }

    @Test
    void processCart_ShouldThrowException_WhenCartNotActive() {
        testCart.setStatus(CartStatus.PROCESSING);

        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testCart));

        assertThrows(InvalidOperationException.class,
                () -> cartService.processCart(1L));

        verify(orderProcessingService, never()).process(any());
    }

    @Test
    void processCart_ShouldThrowException_WhenCartNotFound() {
        when(cartRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> cartService.processCart(1L));
    }
}