package com.challenge.shoppingcart.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RemoveItemRequest {

    @NotNull(message = "El productId es obligatorio")
    private Long productId;
}