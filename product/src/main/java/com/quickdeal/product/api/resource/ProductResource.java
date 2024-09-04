package com.quickdeal.product.api.resource;

import com.quickdeal.common.service.domain.CategoryType;
import com.quickdeal.common.service.domain.Product;
import java.time.Instant;

public record ProductResource(
    Long id,
    CategoryType categoryType,
    String categoryName,
    String name,
    String description,
    Integer price,
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
        product.createdAt(),
        product.updatedAt()
    );
  }
}
