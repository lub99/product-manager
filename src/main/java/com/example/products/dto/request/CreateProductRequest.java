package com.example.products.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Code is required")
        @Size(min = 10, max = 10, message = "Code must be exactly 10 characters")
        String code,

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Price EUR is required")
        @PositiveOrZero(message = "Price EUR must be >= 0")
        @Digits(integer = 17, fraction = 2, message = "Price EUR must have at most 2 decimal places")
        BigDecimal priceEur,

        @NotNull(message = "Availability is required")
        Boolean isAvailable
) {}
