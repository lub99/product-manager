package com.example.products.auth.service;

import com.example.products.auth.service.impl.AuthServiceImpl;
import com.example.products.domain.RefreshToken;
import com.example.products.domain.Role;
import com.example.products.domain.User;
import com.example.products.dto.request.LoginRequest;
import com.example.products.dto.request.RefreshTokenRequest;
import com.example.products.dto.request.RegisterRequest;
import com.example.products.dto.response.AuthResponse;
import com.example.products.exception.EmailAlreadyExistsException;
import com.example.products.exception.InvalidTokenException;
import com.example.products.repository.RefreshTokenRepository;
import com.example.products.repository.UserRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                userRepository, refreshTokenRepository, jwtService,
                authenticationManager, passwordEncoder, new SimpleMeterRegistry());
        ReflectionTestUtils.setField(authService, "accessTokenExpiration", 900L);
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 604800L);
    }

    @Test
    void register_success() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("test.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.register(request);

        assertThat(result.accessToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();

        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_emailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("user@example.com", "password123");

        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_success() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = user(1L, "user@example.com", Role.USER);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("test.access.token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse result = authService.login(request);

        assertThat(result.accessToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_badCredentials() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void refresh_success() {
        User user = user(1L, "user@example.com", Role.USER);
        RefreshToken token = refreshToken(user, Instant.now().plusSeconds(3600), false);
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwtService.generateAccessToken(user)).thenReturn("test.access.token");

        AuthResponse result = authService.refresh(request);

        assertThat(result.accessToken()).isNotNull();
        assertThat(result.refreshToken()).isNotNull();
        assertThat(token.isRevoked()).isTrue();
    }

    @Test
    void refresh_tokenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("nonexistent-token");

        when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void refresh_revokedToken() {
        User user = user(1L, "user@example.com", Role.USER);
        RefreshToken token = refreshToken(user, Instant.now().plusSeconds(3600), true);
        RefreshTokenRequest request = new RefreshTokenRequest("revoked-token");

        when(refreshTokenRepository.findByToken("revoked-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);

        verify(refreshTokenRepository).deleteAllByUserId(1L);
    }

    @Test
    void refresh_expiredToken() {
        User user = user(1L, "user@example.com", Role.USER);
        RefreshToken token = refreshToken(user, Instant.now().minusSeconds(100), false);
        RefreshTokenRequest request = new RefreshTokenRequest("expired-token");

        when(refreshTokenRepository.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void logout_success() {
        User user = user(1L, "user@example.com", Role.USER);
        RefreshToken token = refreshToken(user, Instant.now().plusSeconds(3600), false);
        RefreshTokenRequest request = new RefreshTokenRequest("valid-refresh-token");

        when(refreshTokenRepository.findByToken("valid-refresh-token")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        authService.logout(request);

        assertThat(token.isRevoked()).isTrue();
        verify(refreshTokenRepository).save(token);
    }

    @Test
    void logout_tokenNotFound() {
        RefreshTokenRequest request = new RefreshTokenRequest("nonexistent-token");

        when(refreshTokenRepository.findByToken("nonexistent-token")).thenReturn(Optional.empty());

        authService.logout(request);

        verify(refreshTokenRepository, never()).save(any());
    }

    private User user(Long id, String email, Role role) {
        return User.builder()
                .id(id)
                .email(email)
                .password("encoded-password")
                .role(role)
                .build();
    }

    private RefreshToken refreshToken(User user, Instant expiresAt, boolean revoked) {
        return RefreshToken.builder()
                .id(1L)
                .token("valid-refresh-token")
                .user(user)
                .expiresAt(expiresAt)
                .revoked(revoked)
                .build();
    }
}
