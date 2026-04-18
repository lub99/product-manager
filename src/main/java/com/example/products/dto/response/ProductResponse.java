package com.example.products.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String code,
        String name,
        BigDecimal priceEur,
        BigDecimal priceUsd,
        Boolean isAvailable
) {}
