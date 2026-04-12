package com.challenge.shoppingcart.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCartRequest {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;
}