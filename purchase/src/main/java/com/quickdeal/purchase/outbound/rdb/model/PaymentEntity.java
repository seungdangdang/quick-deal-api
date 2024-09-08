package com.quickdeal.purchase.outbound.rdb.model;

import com.quickdeal.purchase.domain.PaymentStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity(name = "Payment")
@Table(name = "`payment`")
public class PaymentEntity {

  @Id
  @Column(name = "order_id")
  private Long id;

  @OneToOne
  @MapsId
  @JoinColumn(name = "order_id")
  private OrderEntity order;

  @Column(name = "payment_amount", nullable = false)
  private Integer paymentAmount;

  @Column(name = "payment_date")
  private Instant paymentDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "process_status", nullable = false)
  private PaymentStatusType processStatus;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }

  public static PaymentEntity createPayment(OrderEntity order, Integer paymentAmount) {
    PaymentEntity paymentEntity = new PaymentEntity();
    paymentEntity.order = order;
    paymentEntity.paymentAmount = paymentAmount;
    paymentEntity.processStatus = PaymentStatusType.PROCESSING;
    return paymentEntity;
  }
}
