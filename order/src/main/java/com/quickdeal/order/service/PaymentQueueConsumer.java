package com.quickdeal.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.order.domain.QueueMessage;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentQueueConsumer {

  private final ObjectMapper objectMapper;
  private final QueueService queueService;
  private final KafkaTemplate<String, String> kafkaTemplate;

  public PaymentQueueConsumer(ObjectMapper objectMapper, QueueService queueService,
      KafkaTemplate<String, String> kafkaTemplate) {
    this.objectMapper = objectMapper;
    this.queueService = queueService;
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = {"queue"}, groupId = "payment-consumer-group")
  public void consume(String message) {
    try {
      QueueMessage queueMessage = objectMapper.readValue(message, QueueMessage.class);
      Long productId = queueMessage.productId();

      Integer currentCount = queueService.getCurrentPaymentPageUserCount(productId);

      int MAX_PAYMENT_PAGE_USERS = 10;
      if (currentCount < MAX_PAYMENT_PAGE_USERS) {
        queueService.incrementPaymentPageUserCount(productId);
        queueService.updateLastExitedQueueNumber(productId, queueMessage.queueNumber());

      } else {
        kafkaTemplate.send("queue", message);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
