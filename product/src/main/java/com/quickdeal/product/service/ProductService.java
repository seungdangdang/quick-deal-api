package com.quickdeal.product.service;

import com.quickdeal.product.api.resource.ProductDetailResource;
import com.quickdeal.product.api.resource.ProductListResource;
import com.quickdeal.product.infrastructure.entity.Product;
import com.quickdeal.product.infrastructure.repository.ProductRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

  private final ProductRepository productRepository;

  @Autowired
  public ProductService(ProductRepository productRepository) {
    this.productRepository = productRepository;
  }

  public ProductListResource getProductList(Long lastId, Pageable pageable) {
    Slice<Product> productSlice = productRepository.findByCursor(lastId, pageable);
    List<ProductRecord> productList = productSlice.getContent()
        .stream()
        .map(this::toProductRecord)
        .collect(Collectors.toList());

    return new ProductListResource(productList, productSlice.hasNext());
  }

  public ProductDetailResource getProductDetail(Long id) {
    Product product = productRepository.findById(id).orElseThrow();
    ProductRecord productRecord = toProductRecord(product);

    return new ProductDetailResource(productRecord);
  }

  private ProductRecord toProductRecord(Product product) {
    return new ProductRecord(
        product.getId(),
        product.getCategory().getCategoryType(),
        product.getName(),
        product.getDescription(),
        product.getPrice(),
        product.getStockQuantity(),
        product.getCreatedAt(),
        product.getUpdatedAt()
    );
  }
}