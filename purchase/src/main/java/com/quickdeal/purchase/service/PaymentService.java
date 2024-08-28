package com.quickdeal.purchase.service;

import com.quickdeal.common.service.ProductService;
import com.quickdeal.purchase.domain.CheckoutStatus;
import com.quickdeal.purchase.domain.CheckoutStatusType;
import com.quickdeal.purchase.domain.PaymentStatusType;
import com.quickdeal.purchase.infrastructure.entity.PaymentEntity;
import com.quickdeal.purchase.infrastructure.repository.PaymentRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final ProductService productService;

  public PaymentService(PaymentRepository paymentRepository, ProductService productService) {
    this.paymentRepository = paymentRepository;
    this.productService = productService;
  }

  @Transactional
  public CheckoutStatus getCheckoutStatus(Long orderId, Long productId, Integer paymentAmount) {
    try {
      if (productService.hasStockQuantityById(productId)) {
        //TODO: 결제 처리 로직
        return new CheckoutStatus(CheckoutStatusType.CHECKOUT_COMPLETED, orderId,
            paymentAmount);
      }
      return new CheckoutStatus(CheckoutStatusType.ITEM_SOLD_OUT, orderId, null);
    } catch (Exception e) {
      return new CheckoutStatus(CheckoutStatusType.CHECKOUT_ERROR, orderId, null);
    }
  }

  @Transactional
  public void updatePaymentStatus(Long orderId, Instant paymentDate, PaymentStatusType status) {
    paymentRepository.updateOrderPayment(orderId, paymentDate, status);
  }

  @Transactional
  public void createPayment(PaymentEntity entity) {
    paymentRepository.save(entity);
  }
}
