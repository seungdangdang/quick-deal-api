package com.quickdeal.product.infrastructure.repository;

import com.quickdeal.product.infrastructure.entity.ProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
  List<ProductEntity> findTop20ByIdLessThanOrderByIdDesc(Long id);

  @Query("SELECT p.price FROM Product p WHERE p.id = :id")
  int findPriceById(Long id);
}
