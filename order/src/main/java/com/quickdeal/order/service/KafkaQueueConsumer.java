package com.quickdeal.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class KafkaQueueConsumer {

  private final QueueService queueService;

  public KafkaQueueConsumer(QueueService queueService) {
    this.queueService = queueService;
  }

  @KafkaListener(topics = {"queue-1", "queue-2", "queue-3", "queue-4", "queue-5"}
      , groupId = "payment-consumer-group")
  public void consume(ConsumerRecord<String, String> record,
      Acknowledgment acknowledgment) {
    try {
      queueService.processQueueMessage(record.value());
      acknowledgment.acknowledge();
    } catch (MaxUserLimitExceededException e) {
      acknowledgment.nack(Duration.ofMillis(500));  // 0.5초 후에 동일 메시지를 다시 처리하도록 설정
    } catch (JsonProcessingException e) {
      // todo - 요청 파싱 에러에 대한 로직 구현 필요
    } catch (Exception e) {
      // todo - 예상 치 못한 에러에 대해 처리하는 로직 구현 필요
    }
  }
}
