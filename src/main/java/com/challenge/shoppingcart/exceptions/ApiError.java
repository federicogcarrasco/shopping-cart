package com.challenge.shoppingcart.exceptions;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record ApiError(
        int status,
        String error,
        String message,
        LocalDateTime timestamp
) {}