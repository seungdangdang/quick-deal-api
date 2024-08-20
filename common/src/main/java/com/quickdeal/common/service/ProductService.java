package com.quickdeal.common.service;

import com.quickdeal.common.service.domain.Product;
import java.util.List;

public interface ProductService {

  public List<Product> getProductList(Long lastId);
  public Product getProduct(Long id);
  public int getPriceById(Long id);
}
