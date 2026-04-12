package com.challenge.shoppingcart.dtos;

import com.challenge.shoppingcart.entities.CartStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartDto {
    private Long id;
    private String username;
    private CartStatus status;
}