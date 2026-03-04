package com.empresaccutsb.apirest.mapper;

import com.empresaccutsb.apirest.dto.product.ProductRequest;
import com.empresaccutsb.apirest.dto.product.ProductResponse;
import com.empresaccutsb.apirest.model.Product;

public final class ProductMapper {

    private ProductMapper() {}

    public static Product toEntity(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return product;
    }

    public static void apply(Product product, ProductRequest request) {
        product.setNombre(request.nombre());
        product.setDescripcion(request.descripcion());
        product.setPrecio(request.precio());
    }

    public static ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getPrecio(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getCreatedBy(),
                product.getUpdatedBy(),
                product.getDeletedAt(),
                product.getDeletedBy());
    }
}
