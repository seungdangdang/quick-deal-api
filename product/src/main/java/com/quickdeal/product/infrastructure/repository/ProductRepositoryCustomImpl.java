package com.quickdeal.product.infrastructure.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.quickdeal.product.infrastructure.entity.Product;
import com.quickdeal.product.infrastructure.entity.QProduct;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  public ProductRepositoryCustomImpl(JPAQueryFactory queryFactory) {
    this.queryFactory = queryFactory;
  }

  @Override
  public Slice<Product> findByCursor(Long lastId, Pageable pageable) {
    QProduct product = QProduct.product;

    List<Product> products = queryFactory.selectFrom(product)
        .where(lastId != null ? product.id.lt(lastId) : null)
        .orderBy(product.id.desc())
        .limit(pageable.getPageSize() + 1)
        .fetch();

    boolean hasNext = products.size() > pageable.getPageSize();

    if (hasNext) {
      products.remove(products.size() - 1);
    }

    return new SliceImpl<>(products, pageable, hasNext);
  }
}
