package com.example.products.service;

import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.response.ProductResponse;

import java.util.List;

public interface ProductService {
    ProductResponse createProduct(CreateProductRequest request);
    ProductResponse getProductById(Long id);
    List<ProductResponse> getAllProducts();
}
