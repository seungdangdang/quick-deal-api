package com.quickdeal.order.infrastructure.repository;

import com.quickdeal.order.infrastructure.entity.PaymentEntity;
import com.quickdeal.order.infrastructure.entity.PaymentStatus;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE Payment p SET p.processStatus = :status, p.paymentDate = :paymentDate WHERE p.id = :orderId")
  void updateOrderPayment(
      @Param("orderId") Long orderId
      ,@Param("paymentDate") Instant paymentDate
      ,@Param("status") PaymentStatus status);
}
