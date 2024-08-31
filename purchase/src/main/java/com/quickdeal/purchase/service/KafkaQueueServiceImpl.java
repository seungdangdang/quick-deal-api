package com.quickdeal.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.purchase.domain.QueueMessage;
import io.jsonwebtoken.Claims;
import java.util.concurrent.CompletableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueServiceImpl implements MessageQueueProducer {

  private final Logger log;
  private final TokenService tokenService;
  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  public KafkaQueueServiceImpl(TokenService tokenService,
      KafkaTemplate<String, String> kafkaTemplate) {
    this.tokenService = tokenService;
    this.kafkaTemplate = kafkaTemplate;
    this.objectMapper = new ObjectMapper();
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @Override
  public void publishMessage(String topic, QueueMessage message) {
    try {
      String token = message.ticketToken();
      Claims claims = tokenService.validateTokenAndGetClaims(token);
      String messageString = objectMapper.writeValueAsString(message);
      log.info("[publishMessage] message with orderId: {} userId: {}",
          message.userUUID(), claims.get("order_id"));

      CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic,
          messageString);

      future.whenComplete((result, ex) -> {
        if (ex == null) {
          log.info("[publishMessage] Message sent successfully to topic: {}, partition: {}, offset: {}",
              result.getRecordMetadata().topic(),
              result.getRecordMetadata().partition(),
              result.getRecordMetadata().offset());
        } else {
          log.error("[publishMessage] Failed to send message to topic: {}", topic, ex);
        }
      });

//      kafkaTemplate.send(topic, objectMapper.writeValueAsString(message));
    } catch (JsonProcessingException e) {
      // todo - 예외처리
    }
  }
}