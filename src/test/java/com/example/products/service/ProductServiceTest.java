package com.example.products.service;

import com.example.products.client.HnbApiClient;
import com.example.products.domain.Product;
import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.response.ProductResponse;
import com.example.products.exception.DuplicateProductCodeException;
import com.example.products.exception.ProductNotFoundException;
import com.example.products.mapper.ProductMapper;
import com.example.products.repository.ProductRepository;
import com.example.products.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private HnbApiClient hnbApiClient;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_success() {
        CreateProductRequest request = new CreateProductRequest(
                "PROD000001", "Widget", new BigDecimal("9.99"), true);
        Product entity = product(1L, "PROD000001", new BigDecimal("9.99"), new BigDecimal("11.79"));
        ProductResponse response = response(1L, "PROD000001", new BigDecimal("9.99"), new BigDecimal("11.79"));

        when(productRepository.existsByCode("PROD000001")).thenReturn(false);
        when(productMapper.toEntity(request)).thenReturn(entity);
        when(hnbApiClient.getUsdRate()).thenReturn(new BigDecimal("1.179700"));
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponse(entity)).thenReturn(response);

        ProductResponse result = productService.createProduct(request);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.priceUsd()).isEqualByComparingTo("11.79");
        verify(productRepository).save(entity);
    }

    @Test
    void createProduct_duplicateCode_throws() {
        CreateProductRequest request = new CreateProductRequest(
                "PROD000001", "Widget", new BigDecimal("9.99"), true);

        when(productRepository.existsByCode("PROD000001")).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(DuplicateProductCodeException.class)
                .hasMessageContaining("PROD000001");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_found() {
        Product entity = product(1L, "PROD000001", new BigDecimal("9.99"), new BigDecimal("11.79"));
        ProductResponse response = response(1L, "PROD000001", new BigDecimal("9.99"), new BigDecimal("11.79"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productMapper.toResponse(entity)).thenReturn(response);

        assertThat(productService.getProductById(1L)).isEqualTo(response);
    }

    @Test
    void getProductById_notFound_throws() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getAllProducts_delegatesToRepositoryAndMapper() {
        List<Product> entities = List.of(
                product(1L, "PROD000001", new BigDecimal("9.99"), new BigDecimal("11.79")),
                product(2L, "PROD000002", new BigDecimal("5.00"), new BigDecimal("5.90"))
        );
        List<ProductResponse> responses = List.of(
                response(1L, "PROD000001", new BigDecimal("9.99"), new BigDecimal("11.79")),
                response(2L, "PROD000002", new BigDecimal("5.00"), new BigDecimal("5.90"))
        );

        when(productRepository.findAll()).thenReturn(entities);
        when(productMapper.toResponseList(entities)).thenReturn(responses);

        assertThat(productService.getAllProducts()).hasSize(2);
    }

    private Product product(Long id, String code, BigDecimal priceEur, BigDecimal priceUsd) {
        return Product.builder()
                .id(id).code(code).name("Widget")
                .priceEur(priceEur).priceUsd(priceUsd).isAvailable(true)
                .build();
    }

    private ProductResponse response(Long id, String code, BigDecimal priceEur, BigDecimal priceUsd) {
        return new ProductResponse(id, code, "Widget", priceEur, priceUsd, true);
    }
}
