package com.quickdeal.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.exception.OrderStatusInvalidException;
import com.quickdeal.purchase.domain.QueueMessage;
import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.util.List;
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
  private final InMemoryService inMemoryService;
  private final ObjectMapper objectMapper;
  private final List<String> topicList;

  public KafkaQueueConsumer(TokenService tokenService, TicketService queueService,
      InMemoryService inMemoryService, ObjectMapper objectMapper, List<String> topicList) {
    this.tokenService = tokenService;
    this.queueService = queueService;
    this.inMemoryService = inMemoryService;
    this.objectMapper = objectMapper;
    this.topicList = topicList;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @KafkaListener(topics = "#{@topicList}", groupId = "payment-consumer-group")
  public void consume(ConsumerRecord<String, String> record, Acknowledgment acknowledgment)
      throws JsonProcessingException {
    QueueMessage queueMessage = objectMapper.readValue(record.value(), QueueMessage.class);
    String token = queueMessage.ticketToken();
    Claims claims = tokenService.validateTokenAndGetClaims(token);
    log.debug("[consume] message with orderId: {} userId: {}", claims.get("order_id"),
        queueMessage.userId());
    try {
      queueService.validateTicketAndPaymentPageAccessible(queueMessage);
      acknowledgment.acknowledge();
      log.debug("[consume] offset committed successfully with orderId: {} userId: {}",
          claims.get("order_id"), queueMessage.userId());
    } catch (MaxUserLimitExceededException e) {
      acknowledgment.nack(Duration.ofMillis(500));
    } catch (OrderStatusInvalidException e) {
      inMemoryService.updateLastExitedTicketNumber(claims.get("product_id", Long.class),
          claims.get("ticketNumber", Long.class));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
