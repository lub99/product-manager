package com.example.products.exception;

public class DuplicateProductCodeException extends RuntimeException {
    public DuplicateProductCodeException(String code) {
        super("Product with code already exists: " + code);
    }
}
