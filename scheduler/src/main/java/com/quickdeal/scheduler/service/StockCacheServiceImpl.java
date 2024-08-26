package com.quickdeal.scheduler.service;

import com.quickdeal.common.service.StockCacheService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class StockCacheServiceImpl implements StockCacheService {

  private final Map<Long, Integer> stockCache = new ConcurrentHashMap<>();

  public void updateStock(Long productId, int stockQuantity) {
    stockCache.put(productId, stockQuantity);
  }

  public Integer getStock(Long productId) {
    return stockCache.getOrDefault(productId, 0);
  }
}
