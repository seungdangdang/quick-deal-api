package com.quickdeal.purchase.infrastructure.repository;

import com.quickdeal.purchase.infrastructure.entity.OrderEntity;
import com.quickdeal.purchase.domain.OrderStatusType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

  @Modifying
  @Query("UPDATE Order o SET o.processStatus = :status WHERE o.id = :orderId")
  void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") OrderStatusType status);
}