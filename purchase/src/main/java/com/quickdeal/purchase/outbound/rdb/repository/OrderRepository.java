package com.quickdeal.purchase.outbound.rdb.repository;

import com.quickdeal.purchase.outbound.rdb.model.OrderEntity;
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
