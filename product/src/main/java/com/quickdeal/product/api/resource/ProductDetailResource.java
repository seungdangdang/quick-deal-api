package com.quickdeal.product.api.resource;

import com.quickdeal.product.service.ProductRecord;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductDetailResource {

  private ProductRecord productInfo;
}
