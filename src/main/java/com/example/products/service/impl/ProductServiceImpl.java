package com.example.products.service.impl;

import com.example.products.client.HnbApiClient;
import com.example.products.domain.Product;
import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.request.ProductFilter;
import com.example.products.dto.response.ProductResponse;
import com.example.products.exception.DuplicateProductCodeException;
import com.example.products.exception.ProductNotFoundException;
import com.example.products.mapper.ProductMapper;
import com.example.products.repository.ProductRepository;
import com.example.products.repository.ProductSpecifications;
import com.example.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final HnbApiClient hnbApiClient;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByCode(request.code())) {
            throw new DuplicateProductCodeException(request.code());
        }
        Product product = productMapper.toEntity(request);
        BigDecimal rate = hnbApiClient.getUsdRate();
        product.setPriceUsd(request.priceEur().multiply(rate).setScale(2, RoundingMode.HALF_UP));
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public Page<ProductResponse> getAllProducts(ProductFilter filter, Pageable pageable) {
        return productRepository
                .findAll(ProductSpecifications.matches(
                        Objects.requireNonNullElse(filter, ProductFilter.empty())), pageable)
                .map(productMapper::toResponse);
    }
}
