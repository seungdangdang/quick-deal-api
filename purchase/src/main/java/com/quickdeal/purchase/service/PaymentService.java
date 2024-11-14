package com.quickdeal.purchase.service;

import com.quickdeal.common.service.ProductService;
import com.quickdeal.purchase.domain.PaymentStatus;
import com.quickdeal.purchase.domain.PaymentStatusType;
import com.quickdeal.purchase.domain.PaymentStatuses;
import com.quickdeal.purchase.outbound.rdb.model.PaymentEntity;
import com.quickdeal.purchase.outbound.rdb.repository.PaymentRepository;
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

  @Transactional(readOnly = true)
  public PaymentStatus getPaymentStatus(Long orderId, Long productId, Integer paymentAmount) {
    try {
      if (productService.getStockQuantityById(productId) > 0) {
//        //TODO: 결제로직 필요, 현재 임시로 30초~2분의 지연이 있도록 설정

        return new PaymentStatus(PaymentStatuses.PAYMENT_COMPLETED, orderId,
            paymentAmount);
      }
      return new PaymentStatus(PaymentStatuses.ITEM_SOLD_OUT, orderId, null);
    } catch (Exception e) {
      return new PaymentStatus(PaymentStatuses.PAYMENT_ERROR, orderId, null);
    }
  }

  @Transactional
  public void updatePaymentStatus(Long orderId, Instant paymentDate, PaymentStatusType status) {
    paymentRepository.updateOrderPayment(orderId, paymentDate, status);
  }

  @Transactional
  public PaymentEntity createPayment(PaymentEntity entity) {
    return paymentRepository.save(entity);
  }
}
