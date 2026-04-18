package com.example.products.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Code is required")
        @Size(min = 10, max = 10, message = "Code must be exactly 10 characters")
        String code,

        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Price EUR is required")
        @DecimalMin(value = "0.0", message = "Price EUR must be >= 0")
        BigDecimal priceEur,

        @NotNull(message = "Availability is required")
        Boolean isAvailable
) {}
