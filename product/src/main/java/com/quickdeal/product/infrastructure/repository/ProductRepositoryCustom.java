package com.quickdeal.product.infrastructure.repository;

import com.quickdeal.product.infrastructure.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ProductRepositoryCustom {
  Slice<Product> findByCursor(Long lastId, Pageable pageable);
}
