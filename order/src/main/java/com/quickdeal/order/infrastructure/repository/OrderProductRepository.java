package com.quickdeal.order.infrastructure.repository;

import com.quickdeal.order.infrastructure.entity.OrderProductEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {

  // TODO - 주문 1회 상품 개수 제한둘 것
  @Query("SELECT op FROM OrderProduct op WHERE op.order.id = :orderId")
  List<OrderProductEntity> findByOrderId(@Param("orderId") Long orderId);
}
