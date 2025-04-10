package com.quickdeal.purchase.outbound.rdb.repository;

import com.quickdeal.purchase.outbound.rdb.model.OrderProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderProductRepository extends JpaRepository<OrderProductEntity, Long> {

  @Query("SELECT op FROM OrderProduct op WHERE op.order.id = :orderId")
  OrderProductEntity findByOrderId(@Param("orderId") Long orderId);
}
