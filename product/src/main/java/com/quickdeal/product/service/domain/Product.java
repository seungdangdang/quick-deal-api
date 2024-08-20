package com.quickdeal.product.service.domain;

import com.quickdeal.product.infrastructure.entity.CategoryType;
import java.time.Instant;

public record Product(
    Long id,
    CategoryType categoryType,
    String name,
    String description,
    Integer price,
    Integer stockQuantity,
    Instant createdAt,
    Instant updatedAt
) {

}
