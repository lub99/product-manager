package com.example.products.service;

import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.request.ProductFilter;
import com.example.products.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProductById(Long id);

    Page<ProductResponse> getAllProducts(ProductFilter filter, Pageable pageable);
}
