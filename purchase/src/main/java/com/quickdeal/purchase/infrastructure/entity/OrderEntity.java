package com.quickdeal.purchase.infrastructure.entity;

import com.quickdeal.purchase.domain.Order;
import com.quickdeal.purchase.domain.OrderStatusType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Getter;

@Entity(name = "Order")
@Table(name = "`order`")
public class OrderEntity {

  @Getter
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Enumerated(EnumType.STRING)
  @Column(name = "process_status", nullable = false)
  private OrderStatusType processStatus;

  @Column(name = "created_at", nullable = false, updatable = false)
  private final Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt = Instant.now();

  public OrderEntity() {
  }

  @PreUpdate
  public void preUpdate() {
    this.updatedAt = Instant.now();
  }

  @OneToOne(mappedBy = "order")
  private PaymentEntity payment;

  public static OrderEntity createOrder(String userId) {
    OrderEntity order = new OrderEntity();
    order.userId = userId;
    order.processStatus = OrderStatusType.PROCESSING;
    return order;
  }

  public Order toOrder() {
    return new Order(
        id,
        userId,
        processStatus,
        createdAt,
        updatedAt
    );
  }
}
