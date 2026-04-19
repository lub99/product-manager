package com.example.products.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public record ErrorResponse(
        @Schema(description = "HTTP status code")
        int status,

        @Schema(description = "Short error label")
        String error,

        @Schema(description = "Detailed error message")
        String message,

        @Schema(description = "UTC timestamp when error occurred")
        Instant timestamp
) {}
