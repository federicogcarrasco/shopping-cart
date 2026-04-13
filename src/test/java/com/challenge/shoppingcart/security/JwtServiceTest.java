package com.challenge.shoppingcart.security;

import com.challenge.shoppingcart.entities.UserRole;
import com.challenge.shoppingcart.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Seteamos los valores privados que normalmente inyecta @Value
        org.springframework.test.util.ReflectionTestUtils.setField(
                jwtService, "secret",
                "dGVzdC1zZWNyZXQta2V5LWZvci11bml0LXRlc3RpbmctcHVycG9zZXMtb25seQ==");
        org.springframework.test.util.ReflectionTestUtils.setField(
                jwtService, "expiration", 86400000L);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .role(UserRole.USER)
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        String token = jwtService.generateToken(testUser);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        String username = jwtService.extractUsername(token);
        assertEquals(testUser.getUsername(), username);
    }

    @Test
    void extractUserId_ShouldReturnCorrectUserId() {
        String token = jwtService.generateToken(testUser);
        Long userId = jwtService.extractUserId(token);
        assertEquals(testUser.getId(), userId);
    }

    @Test
    void extractRole_ShouldReturnCorrectRole() {
        String token = jwtService.generateToken(testUser);
        String role = jwtService.extractRole(token);
        assertEquals(testUser.getRole().name(), role);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_WhenTokenIsValid() {
        String token = jwtService.generateToken(testUser);
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        String token = jwtService.generateToken(testUser);

        UserDetails otherUser = User.builder()
                .id(2L)
                .username("otheruser")
                .password("password")
                .role(UserRole.USER)
                .build();

        assertFalse(jwtService.isTokenValid(token, otherUser));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_WhenTokenIsExpired() {
        org.springframework.test.util.ReflectionTestUtils.setField(
                jwtService, "expiration", -1000L);

        String expiredToken = jwtService.generateToken(testUser);
        assertFalse(jwtService.isTokenValid(expiredToken, testUser));
    }
}