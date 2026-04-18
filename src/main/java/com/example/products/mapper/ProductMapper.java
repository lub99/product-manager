package com.example.products.mapper;

import com.example.products.domain.Product;
import com.example.products.dto.request.CreateProductRequest;
import com.example.products.dto.response.ProductResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toResponse(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "priceUsd", ignore = true)
    Product toEntity(CreateProductRequest request);

    List<ProductResponse> toResponseList(List<Product> products);
}
