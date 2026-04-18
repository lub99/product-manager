package com.example.products.service.impl;

import com.example.products.domain.Product;
import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.response.ProductResponse;
import com.example.products.exception.DuplicateProductCodeException;
import com.example.products.exception.ProductNotFoundException;
import com.example.products.mapper.ProductMapper;
import com.example.products.repository.ProductRepository;
import com.example.products.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        if (productRepository.existsByCode(request.code())) {
            throw new DuplicateProductCodeException(request.code());
        }
        Product product = productMapper.toEntity(request);
        product.setPriceUsd(BigDecimal.ZERO);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(productMapper::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @Override
    public List<ProductResponse> getAllProducts() {
        return productMapper.toResponseList(productRepository.findAll());
    }
}
