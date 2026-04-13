package com.challenge.shoppingcart.security;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter @Setter
public class AuthResponse {
    private Long userId;
    private String token;
}