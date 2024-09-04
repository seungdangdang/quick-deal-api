package com.quickdeal.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.purchase.domain.QueueMessage;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueConsumer {

  private final Logger log;
  private final TokenService tokenService;
  private final TicketService queueService;
  private final ObjectMapper objectMapper;

  public KafkaQueueConsumer(TokenService tokenService, TicketService queueService,
      ObjectMapper objectMapper) {
    this.tokenService = tokenService;
    this.queueService = queueService;
    this.objectMapper = objectMapper;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @KafkaListener(topics = {"queue-1", "queue-2", "queue-3", "queue-4",
      "queue-5", "queue-51", "queue-47"}, groupId = "payment-consumer-group")
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    log.debug("[consume] message with offset: {}", record.offset());
    try {
      QueueMessage queueMessage = objectMapper.readValue(record.value(), QueueMessage.class);
      String token = queueMessage.ticketToken(); //TODO: ticketToken 은 나중에 삭제할 것
      Claims claims = tokenService.validateTokenAndGetClaims(token); //TODO: ticketToken 은 나중에 삭제할 것
      log.debug("[consume] message with orderId: {} userId: {}", claims.get("order_id"),
          queueMessage.userId());
      queueService.validateTicketAndPaymentPageAccessible(queueMessage);
      acknowledgment.acknowledge();
      log.debug("[consume] offset committed successfully with orderId: {} userId: {}",
          claims.get("order_id"), queueMessage.userId());
    } catch (MaxUserLimitExceededException e) {
      acknowledgment.nack(Duration.ofMillis(500));
    } catch (JsonProcessingException e) {
      // todo - 예외처리
    } catch (InterruptedException e) {
      // todo - 예외처리
    } catch (Exception e) {
      // todo - 예외처리
    }
  }
}
