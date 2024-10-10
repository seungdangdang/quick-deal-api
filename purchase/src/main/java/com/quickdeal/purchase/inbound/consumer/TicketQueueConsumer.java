package com.quickdeal.purchase.inbound.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.purchase.domain.PaymentPageUser;
import com.quickdeal.purchase.domain.QueueMessage;
import com.quickdeal.purchase.service.InMemoryService;
import com.quickdeal.purchase.service.OrderTicketTokenService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class TicketQueueConsumer {

  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private final OrderTicketTokenService orderTicketTokenService;
  private final ObjectMapper objectMapper;
  private final MeterRegistry meterRegistry;
  private final InMemoryService inMemoryService;

  public TicketQueueConsumer(
      OrderTicketTokenService orderTicketTokenService,
      ObjectMapper objectMapper,
      MeterRegistry meterRegistry,
      InMemoryService inMemoryService
  ) {
    this.orderTicketTokenService = orderTicketTokenService;
    this.objectMapper = objectMapper;
    this.meterRegistry = meterRegistry;
    this.inMemoryService = inMemoryService;
  }

  @KafkaListener(topics = "#{@topicList}", groupId = "payment-consumer-group", containerFactory = "kafkaListenerContainerFactory")
  public void consumeBatch(List<ConsumerRecord<String, String>> records, Consumer<?, ?> consumer) {
    if (records.isEmpty()) {
      return;
    }

    // 첫 번째 레코드의 토픽에서 productId를 추출
    String topic = records.get(0).topic();
    long productId = extractProductIdFromTopic(topic);

    log.info("Consuming records for productId: {}", productId);
    processRecords(records, consumer, productId);
  }

  private long extractProductIdFromTopic(String topic) {
    try {
      // 토픽 이름에서 productId 추출
      String[] parts = topic.split("-");
      return Long.parseLong(parts[parts.length - 1]);
    } catch (Exception e) {
      log.error("Failed to extract productId from topic: {}. Defaulting to -1", topic, e);
      return -1;
    }
  }

  private void processRecords(
      List<ConsumerRecord<String, String>> records,
      Consumer<?, ?> consumer,
      long productId
  ) {
    log.debug("[consumeBatch] Consumed {} records", records.size());

    long numberOfPaymentAccessible = inMemoryService.getNumberOfExistsPaymentSlot(productId);
    long processedRecordNumber = 0;
    List<PaymentPageUser> processableRequests = new ArrayList<>();
    long lastProcessedOffset = -1;

    for (ConsumerRecord<String, String> record : records) {
      if (processedRecordNumber >= numberOfPaymentAccessible) {
        log.warn(
            "[consumeBatch] Reached max number of payment slots. Skipping message at offset: {} with userId: {}",
            record.offset(), getUserIdFromRecord(record.value()));
        break;
      }

      QueueMessage queueMessage = null;
      try {
        queueMessage = objectMapper.readValue(record.value(), QueueMessage.class);
        log.debug("[processRecords][{}][{}] Processing record: {}", record.topic(), record.offset(),
            record.value());
      } catch (Exception e) {
        log.error("[processRecords][{}][{}] error - parsing failed", record.topic(),
            record.offset(), e);
        break;
      }

      try {
        orderTicketTokenService.validateTokenAndGetClaims(queueMessage.ticketToken());
      } catch (Exception e) {
        log.error("[processRecords][{}][{}] Token validation failed", record.topic(),
            record.offset(), e);
        break;
      }

      processableRequests.add(
          new PaymentPageUser(queueMessage.ticketNumber(), queueMessage.userId()));
      processedRecordNumber++;
      lastProcessedOffset = record.offset() + 1;
    }

    TopicPartition topicPartition = new TopicPartition(records.get(0).topic(),
        records.get(0).partition());

    // 오프셋 커밋 및 재설정
    try {
      if (lastProcessedOffset > -1) {
        consumer.commitSync(
            Collections.singletonMap(topicPartition, new OffsetAndMetadata(lastProcessedOffset)));
        log.info("[consumeBatch] Successfully committed offset: {} for partition: {}",
            lastProcessedOffset, topicPartition);
      } else {
        log.warn("[consumeBatch] No offsets to commit.");
      }

      consumer.seek(topicPartition,
          lastProcessedOffset > -1 ? lastProcessedOffset : records.get(0).offset());
    } catch (Exception e) {
      log.error("[consumeBatch] Failed to commit or seek offset for partition: {}", topicPartition,
          e);
    }

    boolean result = false;
    if (!processableRequests.isEmpty()) {
      result = inMemoryService.insertPaymentAccessAndUpdateLastProcessed(processableRequests,
          productId);
    }

    meterRegistry.counter("kafka_consume_result", "callee", "[consumer] - " + productId, "status",
        result + "").increment();
  }

  private String getUserIdFromRecord(String recordValue) {
    try {
      QueueMessage queueMessage = objectMapper.readValue(recordValue, QueueMessage.class);
      return queueMessage.userId();
    } catch (Exception e) {
      log.error("[getUserIdFromRecord] Failed to extract userId from record: {}", e.getMessage());
      return "Unknown";
    }
  }
}
