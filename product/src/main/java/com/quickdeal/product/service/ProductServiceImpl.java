package com.quickdeal.product.service;

import com.quickdeal.common.exception.NotFoundException;
import com.quickdeal.common.service.ProductService;
import com.quickdeal.common.service.StockCacheService;
import com.quickdeal.common.service.domain.Product;
import com.quickdeal.product.outbound.rdb.model.ProductEntity;
import com.quickdeal.product.outbound.rdb.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

  private final ProductRepository productRepository;
  private final StockCacheService stockCacheService;

  public ProductServiceImpl(ProductRepository productRepository,
      StockCacheService stockCacheService) {
    this.productRepository = productRepository;
    this.stockCacheService = stockCacheService;
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> getProdudctList() {
    return productRepository.findAll().stream().map(ProductEntity::toProduct).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Product> getProductList(Long lastId) {
    return productRepository.findTop20ByIdLessThanOrderByIdDesc(lastId).stream()
        .map(ProductEntity::toProduct).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Product getProduct(Long id) {
    ProductEntity entity = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("존재하지 않는 상품 아이디입니다 : " + id));
    return entity.toProduct();
  }

  @Override
  @Transactional(readOnly = true)
  public int getPriceById(Long id) {
    Product product = productRepository.findById(id)
        .orElseThrow(() -> new NotFoundException("해당 상품 아이디는 존재하지 않습니다.")).toProduct();

    return product.price();
  }

  @Override
  @Transactional(readOnly = true)
  public long getStockQuantityById(Long id) {
    return productRepository.findStockQuantityById(id)
        .orElseThrow(() -> new NotFoundException("해당 상품 아이디는 존재하지 않습니다."));
  }

  @Override
  public boolean hasCachingStockQuantityById(Long id) {
    Integer stock = stockCacheService.getStock(id);
    if (stock == null) {
      throw new NotFoundException("해당 상품 아이디는 존재하지 않습니다.");
    }

    return stock > 0;
  }

  @Override
  @Transactional
  public void decreaseStockQuantityById(Long productId, int quantity) {
    productRepository.decreaseStockQuantityById(productId, quantity);
  }

  @Override
  @Transactional
  public void increaseStockQuantityById(Long productId) {
    productRepository.increaseStockQuantity(productId);
  }
}
