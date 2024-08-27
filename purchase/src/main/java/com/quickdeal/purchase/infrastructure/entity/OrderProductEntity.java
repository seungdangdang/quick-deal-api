package com.quickdeal.purchase.infrastructure.entity;

import com.quickdeal.purchase.domain.OrderProduct;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity(name = "OrderProduct")
@Table(name = "`order_product`")
public class OrderProductEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private OrderEntity order;

  @Column(name = "product_id", nullable = false)
  private Long productId; // Q - ProductEntity 를 매핑하지 말고 단순히 productId만 저장하고자 함

  @Getter
  @Column(nullable = false)
  private Integer quantity;

  @Getter
  @Column(nullable = false)
  private Integer price;

  public OrderProductEntity() {
  }

  public static OrderProductEntity createOrderProduct(OrderEntity order, Long productId,
      int quantity, int price) {
    OrderProductEntity orderProductEntity = new OrderProductEntity();
    orderProductEntity.order = order;
    orderProductEntity.productId = productId;
    orderProductEntity.quantity = quantity;
    orderProductEntity.price = price;
    return orderProductEntity;
  }

  public OrderProduct toOrderProduct() {
    return new OrderProduct(id, productId, quantity, price);
  }
}
