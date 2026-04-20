package com.example.products.auth.service.impl;

import com.example.products.auth.service.AuthService;
import com.example.products.auth.service.JwtService;
import com.example.products.domain.RefreshToken;
import com.example.products.domain.Role;
import com.example.products.domain.User;
import com.example.products.dto.request.LoginRequest;
import com.example.products.dto.request.RefreshTokenRequest;
import com.example.products.dto.request.RegisterRequest;
import com.example.products.dto.response.AuthResponse;
import com.example.products.exception.EmailAlreadyExistsException;
import com.example.products.exception.InvalidTokenException;
import com.example.products.exception.UserNotFoundException;
import com.example.products.repository.RefreshTokenRepository;
import com.example.products.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = User.builder()
            .email(request.email())
            .password(passwordEncoder.encode(request.password()))
            .role(Role.USER)
            .build();
        userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new UserNotFoundException(request.email()));
        return buildAuthResponse(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken existing = refreshTokenRepository.findByToken(request.refreshToken())
            .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (existing.isRevoked()) {
            refreshTokenRepository.deleteAllByUserId(existing.getUser().getId());
            throw new InvalidTokenException("Refresh token has been revoked");
        }

        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new InvalidTokenException("Refresh token has expired");
        }

        existing.setRevoked(true);
        refreshTokenRepository.save(existing);

        return buildAuthResponse(existing.getUser());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken())
            .ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user);
        RefreshToken refreshToken = RefreshToken.builder()
            .token(UUID.randomUUID().toString())
            .user(user)
            .expiresAt(Instant.now().plusSeconds(refreshTokenExpiration))
            .revoked(false)
            .build();
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(accessToken, refreshToken.getToken(), "Bearer", accessTokenExpiration);
    }
}
