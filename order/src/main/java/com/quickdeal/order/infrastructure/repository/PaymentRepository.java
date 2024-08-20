package com.quickdeal.order.infrastructure.repository;

import com.quickdeal.order.infrastructure.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

}
