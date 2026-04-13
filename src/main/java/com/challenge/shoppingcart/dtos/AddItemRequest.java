package com.challenge.shoppingcart.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddItemRequest {

    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotNull(message = "La quantity es obligatoria")
    @Positive(message = "La quantity debe ser mayor a 0")
    private Integer quantity;
}