package com.example.products.controller;

import com.example.products.client.HnbApiClient;
import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.request.RegisterRequest;
import com.example.products.dto.response.AuthResponse;
import com.example.products.dto.response.ProductResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
class ProductControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @MockBean
    HnbApiClient hnbApiClient;

    private String accessToken;

    @BeforeEach
    void setUp() {
        AuthResponse auth = restTemplate.postForEntity(
                "/auth/register",
                new RegisterRequest("admin@example.com", "password123"),
                AuthResponse.class).getBody();
        accessToken = auth.accessToken();
    }

    @Test
    void createProduct_returns201() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(validRequest("PROD000001"), authHeaders()),
                ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().priceUsd()).isEqualByComparingTo("11.80");
    }

    @Test
    void createProduct_duplicateCode_returns409() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));
        restTemplate.exchange("/api/products", HttpMethod.POST,
                new HttpEntity<>(validRequest("PROD000002"), authHeaders()), ProductResponse.class);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(validRequest("PROD000002"), authHeaders()),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createProduct_invalidCodeLength_returns400() {
        CreateProductRequest request = new CreateProductRequest("SHORT", "Widget", new BigDecimal("9.99"), true);

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(request, authHeaders()),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getProductById_returns200() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));
        ProductResponse created = restTemplate.exchange(
                "/api/products", HttpMethod.POST,
                new HttpEntity<>(validRequest("PROD000003"), authHeaders()),
                ProductResponse.class).getBody();

        ResponseEntity<ProductResponse> response = restTemplate.exchange(
                "/api/products/{id}", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                ProductResponse.class, created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().code()).isEqualTo("PROD000003");
    }

    @Test
    void getProductById_notFound_returns404() {
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/products/99999", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllProducts_returns200() {
        ResponseEntity<ProductResponse[]> response = restTemplate.exchange(
                "/api/products", HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private CreateProductRequest validRequest(String code) {
        return new CreateProductRequest(code, "Widget", new BigDecimal("10.00"), true);
    }
}
