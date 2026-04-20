package com.example.products.auth.controller;

import com.example.products.client.HnbApiClient;
import com.example.products.dto.request.LoginRequest;
import com.example.products.dto.request.RefreshTokenRequest;
import com.example.products.dto.request.RegisterRequest;
import com.example.products.dto.response.AuthResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "jwt.secret-key=test-secret-key-at-least-32-characters-long")
class AuthControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Mock
    HnbApiClient hnbApiClient;

    @Test
    void register_returns201() {
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/register", registerRequest("user1@example.com"), AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().accessToken()).isNotNull();
        assertThat(response.getBody().refreshToken()).isNotNull();
    }

    @Test
    void register_duplicateEmail_returns409() {
        restTemplate.postForEntity("/auth/register", registerRequest("dup@example.com"), AuthResponse.class);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/auth/register", registerRequest("dup@example.com"), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void register_invalidEmail_returns400() {
        RegisterRequest request = new RegisterRequest("not-an-email", "password123");

        ResponseEntity<Void> response = restTemplate.postForEntity("/auth/register", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void login_validCredentials_returns200() {
        restTemplate.postForEntity("/auth/register", registerRequest("login@example.com"), AuthResponse.class);

        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
                "/auth/login",
                new LoginRequest("login@example.com", "password123"),
                AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().accessToken()).isNotNull();
        assertThat(response.getBody().refreshToken()).isNotNull();
    }

    @Test
    void login_invalidPassword_returns401() {
        restTemplate.postForEntity("/auth/register", registerRequest("loginbad@example.com"), AuthResponse.class);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/auth/login",
                new LoginRequest("loginbad@example.com", "wrong-password"),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void refresh_validToken_returns200() {
        AuthResponse registered = restTemplate.postForEntity(
                "/auth/register", registerRequest("refresh@example.com"), AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(registered.accessToken());
        HttpEntity<RefreshTokenRequest> entity = new HttpEntity<>(
                new RefreshTokenRequest(registered.refreshToken()), headers);

        ResponseEntity<AuthResponse> response = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST, entity, AuthResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().accessToken()).isNotNull();
        assertThat(response.getBody().refreshToken()).isNotNull();
    }

    @Test
    void refresh_revokedToken_returns401() {
        AuthResponse registered = restTemplate.postForEntity(
                "/auth/register", registerRequest("refresh2@example.com"), AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(registered.accessToken());
        HttpEntity<RefreshTokenRequest> firstEntity = new HttpEntity<>(
                new RefreshTokenRequest(registered.refreshToken()), headers);

        ResponseEntity<AuthResponse> firstResponse = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST, firstEntity, AuthResponse.class);
        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        HttpEntity<RefreshTokenRequest> secondEntity = new HttpEntity<>(
                new RefreshTokenRequest(registered.refreshToken()), headers);

        ResponseEntity<Void> secondResponse = restTemplate.exchange(
                "/auth/refresh", HttpMethod.POST, secondEntity, Void.class);

        assertThat(secondResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void logout_returns204() {
        AuthResponse registered = restTemplate.postForEntity(
                "/auth/register", registerRequest("logout@example.com"), AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(registered.accessToken());
        HttpEntity<RefreshTokenRequest> entity = new HttpEntity<>(
                new RefreshTokenRequest(registered.refreshToken()), headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/auth/logout", HttpMethod.POST, entity, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void products_withoutToken_returns401() {
        ResponseEntity<Void> response = restTemplate.getForEntity(
                "/api/v1/products", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void products_withValidToken_returns200() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));

        AuthResponse registered = restTemplate.postForEntity(
                "/auth/register", registerRequest("products@example.com"), AuthResponse.class).getBody();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(registered.accessToken());
        HttpEntity<Void> entity = new HttpEntity<>(null, headers);

        ResponseEntity<Object> response = restTemplate.exchange(
                "/api/v1/products", HttpMethod.GET, entity, Object.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private RegisterRequest registerRequest(String email) {
        return new RegisterRequest(email, "password123");
    }
}
