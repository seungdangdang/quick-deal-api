package com.quickdeal.product.api.resource;

import com.quickdeal.product.infrastructure.entity.CategoryType;
import com.quickdeal.product.service.domain.Product;
import java.time.Instant;

public record ProductResource(
    Long id,
    CategoryType categoryType,
    String categoryName,
    String name,
    String description,
    Integer price,
    Integer stockQuantity,
    Instant createdAt,
    Instant updatedAt
) {

  public static ProductResource from(Product product) {
    return new ProductResource(
        product.id(),
        product.categoryType(),
        product.categoryType().getName(),
        product.name(),
        product.description(),
        product.price(),
        product.stockQuantity(),
        product.createdAt(),
        product.updatedAt()
    );
  }
}
