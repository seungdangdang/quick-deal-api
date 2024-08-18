package com.quickdeal.product.service;

import com.quickdeal.common.exception.NotFoundException;
import com.quickdeal.product.infrastructure.entity.ProductEntity;
import com.quickdeal.product.infrastructure.repository.ProductRepository;
import com.quickdeal.product.service.domain.Product;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Transactional(readOnly = true)
  public List<Product> getProductList(Long lastId) {
    return productRepository.findTop20ByIdLessThanOrderByIdDesc(lastId)
        .stream()
        .map(ProductEntity::toProduct)
        .toList();
  }

  @Transactional(readOnly = true)
  public Product getProductDetail(Long id) {
    ProductEntity entity = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("존재하지 않는 상품 아이디입니다 : " + id));
    return entity.toProduct();
  }
}
