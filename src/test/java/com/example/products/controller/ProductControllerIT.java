package com.example.products.controller;

import com.example.products.client.HnbApiClient;
import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.response.ProductResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class ProductControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    TestRestTemplate restTemplate;

    @Mock
    HnbApiClient hnbApiClient;

    @Test
    void createProduct_returns201() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));

        ResponseEntity<ProductResponse> response = restTemplate.postForEntity(
                "/api/products", validRequest("PROD000001"), ProductResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().id()).isNotNull();
        assertThat(response.getBody().priceUsd()).isEqualByComparingTo("11.80");
    }

    @Test
    void createProduct_duplicateCode_returns409() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));
        restTemplate.postForEntity("/api/products", validRequest("PROD000002"), ProductResponse.class);

        ResponseEntity<Void> response = restTemplate.postForEntity(
                "/api/products", validRequest("PROD000002"), Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void createProduct_invalidCodeLength_returns400() {
        CreateProductRequest request = new CreateProductRequest("SHORT", "Widget", new BigDecimal("9.99"), true);

        ResponseEntity<Void> response = restTemplate.postForEntity("/api/products", request, Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getProductById_returns200() {
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.18"));
        ProductResponse created = restTemplate.postForEntity(
                "/api/products", validRequest("PROD000003"), ProductResponse.class).getBody();

        ResponseEntity<ProductResponse> response = restTemplate.getForEntity(
                "/api/products/{id}", ProductResponse.class, created.id());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().code()).isEqualTo("PROD000003");
    }

    @Test
    void getProductById_notFound_returns404() {
        ResponseEntity<Void> response = restTemplate.getForEntity("/api/products/99999", Void.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllProducts_returns200() {
        ResponseEntity<ProductResponse[]> response = restTemplate.getForEntity(
                "/api/products", ProductResponse[].class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    private CreateProductRequest validRequest(String code) {
        return new CreateProductRequest(code, "Widget", new BigDecimal("10.00"), true);
    }
}
