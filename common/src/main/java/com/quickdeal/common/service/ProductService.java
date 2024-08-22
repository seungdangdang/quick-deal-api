package com.quickdeal.common.service;

import com.quickdeal.common.service.domain.Product;
import java.util.List;

public interface ProductService {

  List<Product> getProductList(Long lastId);
  Product getProduct(Long id);
  int getPriceById(Long id);
}
