package com.quickdeal.purchase.inbound.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickdeal.common.exception.MaxUserLimitExceededException;
import com.quickdeal.common.exception.OrderStatusInvalidException;
import com.quickdeal.purchase.domain.QueueMessage;
import com.quickdeal.purchase.outbound.redis.repository.OrderPaymentSlotRedisRepository;
import com.quickdeal.purchase.service.OrderTicketService;
import com.quickdeal.purchase.service.OrderTicketTokenService;
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
public class TicketQueueConsumer {

  private final Logger log;
  private final OrderTicketTokenService orderTicketTokenService;
  private final OrderTicketService queueService;
  private final OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository;
  private final ObjectMapper objectMapper;
  private final List<String> topicList;

  public TicketQueueConsumer(OrderTicketTokenService orderTicketTokenService,
      OrderTicketService queueService,
      OrderPaymentSlotRedisRepository orderPaymentSlotRedisRepository, ObjectMapper objectMapper,
      List<String> topicList) {
    this.orderTicketTokenService = orderTicketTokenService;
    this.queueService = queueService;
    this.orderPaymentSlotRedisRepository = orderPaymentSlotRedisRepository;
    this.objectMapper = objectMapper;
    this.topicList = topicList;
    this.log = LoggerFactory.getLogger(this.getClass());
  }

  @KafkaListener(topics = "#{@topicList}", groupId = "payment-consumer-group")
  public void consume0(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
    consume(0, record, acknowledgment);
  }

  private void consume(int consumerNumber, ConsumerRecord<String, String> record,
      Acknowledgment acknowledgment) {
    log.debug("[consume{}][{}][{}({})] consumed recordValue={}",
        consumerNumber,
        record.topic(),
        record.partition(),
        record.offset(),
        record.value()
    );
    QueueMessage queueMessage = null;
    try {
      queueMessage = objectMapper.readValue(record.value(), QueueMessage.class);
    } catch (Exception e) {
      log.error("[consume{}][{}][{}] error - parsing failed",
          consumerNumber,
          record.topic(),
          record.offset());
      acknowledgment.acknowledge();
      return;
    }
    String token = queueMessage.ticketToken();
    Claims claims = orderTicketTokenService.validateTokenAndGetClaims(token);
    log.debug("[consume{}][{}][{}] userId: {}",
        consumerNumber,
        record.topic(),
        record.offset(),
        queueMessage.userId()
    );
    try {
      queueService.validateTicketAndPaymentPageAccessible(queueMessage);
      acknowledgment.acknowledge();
      log.debug("[consume{}] offset committed successfully with orderId: {} userId: {}",
          consumerNumber,
          claims.get("order_id"), queueMessage.userId());
    } catch (MaxUserLimitExceededException e) {
      acknowledgment.nack(Duration.ofMillis(500));
    } catch (OrderStatusInvalidException e) {
      orderPaymentSlotRedisRepository.updateLastExitedTicketNumber(
          claims.get("product_id", Long.class),
          claims.get("ticketNumber", Long.class));
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
