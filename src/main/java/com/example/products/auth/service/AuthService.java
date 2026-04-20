package com.example.products.auth.service;

import com.example.products.dto.request.LoginRequest;
import com.example.products.dto.request.RefreshTokenRequest;
import com.example.products.dto.request.RegisterRequest;
import com.example.products.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshTokenRequest request);
    void logout(RefreshTokenRequest request);
}
