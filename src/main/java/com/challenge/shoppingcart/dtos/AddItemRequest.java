package com.challenge.shoppingcart.dtos;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class AddItemRequest {

    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotNull(message = "Quantity es obligatoria")
    @Positive(message = "Quantity debe ser mayor a 0")
    @Max(value = 10, message = "Quantity no puede ser mayor a 10")
    private Integer quantity;
}