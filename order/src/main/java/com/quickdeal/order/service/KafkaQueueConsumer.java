package com.quickdeal.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.order.domain.QueueMessage;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueConsumer {

  private final TicketService queueService;
  private final ObjectMapper objectMapper;

  public KafkaQueueConsumer(TicketService queueService, ObjectMapper objectMapper) {
    this.queueService = queueService;
    this.objectMapper = objectMapper;
  }

  @KafkaListener(topics = {"queue-1", "queue-2", "queue-3", "queue-4", "queue-5"}
      , groupId = "payment-consumer-group")
  public void consume(
      ConsumerRecord<String, String> record,
      Acknowledgment acknowledgment
  ) {
    try {
      QueueMessage queueMessage = objectMapper.readValue(record.value(), QueueMessage.class);
      queueService.validateTicketAndPaymentPageAccessible(queueMessage);
      acknowledgment.acknowledge();
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
