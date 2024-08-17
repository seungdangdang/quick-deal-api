package com.quickdeal.product.api.resource;

import com.quickdeal.product.service.ProductRecord;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ProductListResource {

  private List<ProductRecord> products;
  private Boolean hasNext;
}
