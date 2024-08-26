package com.quickdeal.purchase.infrastructure.repository;

import com.quickdeal.purchase.infrastructure.entity.PaymentEntity;
import com.quickdeal.purchase.domain.PaymentStatusType;
import java.time.Instant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

  @Modifying
  @Query("UPDATE Payment p SET p.processStatus = :status, p.paymentDate = :paymentDate WHERE p.id = :orderId")
  void updateOrderPayment(
      @Param("orderId") Long orderId
      ,@Param("paymentDate") Instant paymentDate
      ,@Param("status") PaymentStatusType status);
}
