package com.quickdeal.scheduler.job;

import com.quickdeal.common.service.ProductService;
import com.quickdeal.common.service.domain.Product;
import java.util.List;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class StockScheduler {

  private final ProductService productService;
  private final com.quickdeal.scheduler.service.StockCacheServiceImpl stockCacheService;

  public StockScheduler(ProductService productService,
      com.quickdeal.scheduler.service.StockCacheServiceImpl stockCacheService) {
    this.productService = productService;
    this.stockCacheService = stockCacheService;
  }

  @Async
  @Scheduled(fixedRate = 60000) // 60초마다 실행
  public void updateStockCache() {
    List<Product> products = productService.getProdudctList();
    for (Product product : products) {
      int stockQuantity = product.stockQuantity();
      stockCacheService.updateStock(product.id(), stockQuantity);
    }
  }
}
