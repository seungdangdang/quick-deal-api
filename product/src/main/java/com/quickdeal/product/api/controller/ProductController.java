package com.quickdeal.product.api.controller;

import com.quickdeal.product.api.resource.ProductDetailResource;
import com.quickdeal.product.api.resource.ProductListResource;
import com.quickdeal.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

  private final ProductService productService;

  @Autowired
  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/products")
  public ProductListResource getProducts(@RequestParam(value = "lastId", required = false) Long lastId,
      Pageable pageable) {
    return productService.getProductList(lastId, pageable);
  }

  @GetMapping("detail/{productId}")
  public ProductDetailResource getProductDetail(@PathVariable Long productId) {
    return productService.getProductDetail(productId);
  }
}
