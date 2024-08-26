package com.quickdeal.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.order.domain.QueueMessage;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueConsumer {

  private final QueueService queueService;
  private final ObjectMapper objectMapper;
  private final Logger log;

  public KafkaQueueConsumer(QueueService queueService, ObjectMapper objectMapper) {
    this.queueService = queueService;
    this.objectMapper = objectMapper;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @KafkaListener(topics = {"queue-1", "queue-2", "queue-3", "queue-4", "queue-5"}
      , groupId = "payment-consumer-group")
  public void consume(
      ConsumerRecord<String, String> record,
      Acknowledgment acknowledgment
  ) {
    try {
      QueueMessage queueMessage = objectMapper.readValue(record.value(), QueueMessage.class);
      queueService.processQueueMessageForPageAccess(queueMessage);
      acknowledgment.acknowledge();
    } catch (MaxUserLimitExceededException e) {
      acknowledgment.nack(Duration.ofMillis(500));
    } catch (JsonProcessingException e) {
      // todo - 요청 파싱 에러에 대한 로직 구현 필요
    } catch (InterruptedException e) {
      // todo - 처리 필요
    } catch (Exception e) {
      // todo - 예상 치 못한 에러에 대해 처리하는 로직 구현 필요
    }
  }
}
