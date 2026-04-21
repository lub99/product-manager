package com.example.products.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Optional query filters; open price intervals omit from and/or to as needed")
public record ProductFilter(

        @Schema(description = "Minimum EUR price (inclusive); omit for open lower bound")
        BigDecimal priceEurFrom,

        @Schema(description = "Maximum EUR price (inclusive); omit for open upper bound")
        BigDecimal priceEurTo,

        @Schema(description = "Minimum USD price (inclusive); omit for open lower bound")
        BigDecimal priceUsdFrom,

        @Schema(description = "Maximum USD price (inclusive); omit for open upper bound")
        BigDecimal priceUsdTo,

        @Schema(description = "Case-insensitive partial match on product code (SQL LIKE)")
        String code,

        @Schema(description = "Case-insensitive partial match on product name (SQL LIKE)")
        String name,

        @Schema(description = "Filter by availability; omit to ignore")
        Boolean isAvailable
) {

    public static ProductFilter empty() {
        return new ProductFilter(null, null, null, null, null, null, null);
    }
}
