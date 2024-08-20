package com.quickdeal.common.service.domain;

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
