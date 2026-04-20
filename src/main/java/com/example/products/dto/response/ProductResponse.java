package com.example.products.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ProductResponse(
        @Schema(description = "Auto-generated database ID")
        Long id,

        @Schema(description = "Unique product code")
        String code,

        @Schema(description = "Product name")
        String name,

        @Schema(description = "Price in EUR as stored")
        BigDecimal priceEur,

        @Schema(description = "Price converted to USD via HNB exchange rate")
        BigDecimal priceUsd,

        @Schema(description = "Product availability flag")
        Boolean isAvailable
) { }
