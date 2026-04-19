package com.example.products.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateProductRequest(
        @Schema(description = "Unique product code, exactly 10 characters", example = "PROD-000001")
        @NotBlank(message = "Code is required")
        @Size(min = 10, max = 10, message = "Code must be exactly 10 characters")
        String code,

        @Schema(description = "Human-readable product name", example = "Laptop Pro")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Price in EUR, max 2 decimal places", example = "999.99")
        @NotNull(message = "Price EUR is required")
        @PositiveOrZero(message = "Price EUR must be >= 0")
        @Digits(integer = 17, fraction = 2, message = "Price EUR must have at most 2 decimal places")
        BigDecimal priceEur,

        @Schema(description = "Whether the product is currently available", example = "true")
        @NotNull(message = "Availability is required")
        Boolean isAvailable
) {}
