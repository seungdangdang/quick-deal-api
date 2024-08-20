package com.quickdeal.product.api.controller;

import com.quickdeal.product.api.resource.ProductResource;
import com.quickdeal.product.api.resource.ProductResourceList;
import com.quickdeal.product.service.ProductService;
import com.quickdeal.product.service.domain.Product;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductController {

  private final ProductService productService;

  public ProductController(ProductService productService) {
    this.productService = productService;
  }

  @GetMapping("/products")
  public ProductResourceList getProducts(ProductsRequestParams requestParams) {
    List<Product> products = productService.getProductList(requestParams.lastId());
    List<ProductResource> resources = products.stream().map(ProductResource::from).toList();

    return new ProductResourceList(resources);
  }

  @GetMapping("/products/{productId}")
  public ProductResource getProductDetail(@PathVariable Long productId) {
    Product product = productService.getProductDetail(productId);
    return ProductResource.from(product);
  }
}
