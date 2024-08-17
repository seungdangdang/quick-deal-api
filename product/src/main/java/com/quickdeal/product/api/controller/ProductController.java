package com.quickdeal.product.api.controller;

import com.quickdeal.product.api.resource.ProductDetailResource;
import com.quickdeal.product.api.resource.ProductListResource;
import com.quickdeal.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<ProductListResource> getProducts(@RequestParam(value = "lastId", required = false) Long lastId,
      Pageable pageable) {

    if (lastId != null && lastId < 0) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

    ProductListResource productListResource = productService.getProductList(lastId, pageable);

    if (productListResource == null || productListResource.getProducts().isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }

    return ResponseEntity.status(HttpStatus.OK).body(productListResource);
  }

  @GetMapping("detail/{productId}")
  public ResponseEntity<ProductDetailResource> getProductDetail(@PathVariable Long productId) {

    ProductDetailResource productDetailResource = productService.getProductDetail(productId);
    if (productDetailResource == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
    }
    return ResponseEntity.status(HttpStatus.OK).body(productDetailResource);
  }
}
