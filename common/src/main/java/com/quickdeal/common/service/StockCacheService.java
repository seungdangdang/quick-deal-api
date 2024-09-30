package com.quickdeal.common.service;

public interface StockCacheService {

  void updateStock(Long productId, int stockQuantity);

  Integer getStock(Long productId);
}
