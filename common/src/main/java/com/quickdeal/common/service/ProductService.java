package com.quickdeal.common.service;

import com.quickdeal.common.service.domain.Product;
import java.util.List;

public interface ProductService {

  List<Product> getProdudctList();

  List<Product> getProductList(Long lastId);

  Product getProduct(Long id);

  int getPriceById(Long id);

  boolean hasStockQuantityById(Long id);

  boolean hasCachingStockQuantityById(Long id);

  void decreaseStockQuantityById(Long id);

  void increaseStockQuantityById(Long id);
}
