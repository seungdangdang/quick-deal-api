package com.quickdeal.product.service;

import com.quickdeal.product.infrastructure.entity.CategoryType;
import java.time.LocalDateTime;

public record ProductRecord(
    Long id,
    CategoryType categoryType,
    String name,
    String description,
    int price,
    int stockQuantity,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

  public ProductRecord(CategoryType categoryType, String name, String description, int price, int stockQuantity) {
    this(null, categoryType, name, description, price, stockQuantity, LocalDateTime.now(), LocalDateTime.now());
  }

  public String getCategoryKoreanName() {
    return categoryType.getKoreanName();
  }

  public ProductRecord update(String name, String description, int price, int stockQuantity) {
    return new ProductRecord(
        this.id,
        this.categoryType,
        name,
        description,
        price,
        stockQuantity,
        this.createdAt,
        LocalDateTime.now()
    );
  }
}
