package com.quickdeal.product.outbound.rdb.repository;

import com.quickdeal.product.outbound.rdb.model.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

  List<ProductEntity> findTop20ByIdLessThanOrderByIdDesc(Long id);

  @Modifying
  @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity - 1 WHERE p.id = :productId AND p.stockQuantity > 0")
  void decreaseStockQuantity(@Param("productId") Long productId);

  @Modifying
  @Query("UPDATE Product p SET p.stockQuantity = p.stockQuantity + 1 WHERE p.id = :productId AND p.stockQuantity > 0")
  void increaseStockQuantity(@Param("productId") Long productId);
}
