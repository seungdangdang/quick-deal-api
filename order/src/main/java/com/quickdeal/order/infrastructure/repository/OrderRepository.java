package com.quickdeal.order.infrastructure.repository;

import com.quickdeal.order.infrastructure.entity.OrderEntity;
import com.quickdeal.order.infrastructure.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE Order o SET o.processStatus = :status WHERE o.id = :orderId")
  void updateOrderStatus(@Param("orderId") Long orderId, @Param("status") OrderStatus status);
}
