package com.quickdeal.product.api.controller;

import java.util.Objects;

public record ProductsRequestParams(Long lastId) {
  public ProductsRequestParams(Long lastId) {
    this.lastId = Objects.requireNonNullElse(lastId, Long.MAX_VALUE);
  }
}
