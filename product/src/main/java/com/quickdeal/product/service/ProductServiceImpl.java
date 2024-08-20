package com.quickdeal.product.service;

import com.quickdeal.common.exception.NotFoundException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.common.service.domain.Product;
import com.quickdeal.product.infrastructure.entity.ProductEntity;
import com.quickdeal.product.infrastructure.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;

  public ProductServiceImpl(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> getProductList(Long lastId) {
    return productRepository.findTop20ByIdLessThanOrderByIdDesc(lastId)
        .stream()
        .map(ProductEntity::toProduct)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Product getProduct(Long id) {
    ProductEntity entity = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("존재하지 않는 상품 아이디입니다 : " + id));
    return entity.toProduct();
  }

  @Override
  public int getPriceById(Long id) {
    return productRepository.findPriceById(id);
  }
}
