package com.quickdeal.order.service;

import static com.quickdeal.order.config.RedisConfig.LAST_EXITED_QUEUE_NUMBER_KEY;
import static com.quickdeal.order.config.RedisConfig.LAST_QUEUE_NUMBER_KEY;

import com.quickdeal.order.api.resource.QueuePollingCommand;
import com.quickdeal.order.service.domain.QueuePolling;
import com.quickdeal.order.util.JWTUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class QueueService {

  private final RedisTemplate<String, String> redisTemplate;
  private final JWTUtil jwtUtil;

  public QueueService(RedisTemplate<String, String> redisTemplate, JWTUtil jwtUtil) {
    this.redisTemplate = redisTemplate;
    this.jwtUtil = jwtUtil;
  }

  // TODO - 현재 결제페이지 접속자 수 가져오기
  // TODO - 결제페이지 접속자 수 더하기
  // TODO - 결제페이지 접속자 수 빼기

  // 레디스에서 마지막으로 요청 들어온 대기번호 업데이트하는 코드
  public Long getNewQueueNumber() {
    return redisTemplate.opsForValue().increment(LAST_QUEUE_NUMBER_KEY);
  }

  // TODO - 제품 별 key 필요
  // 레디스에서 마지막 요청 들어온 대기번호 가져오는 코드
  public Long getLastQueueNumber() {
    String lastQueueNumberStr = redisTemplate.opsForValue().get(LAST_QUEUE_NUMBER_KEY);
    return lastQueueNumberStr != null ? Long.parseLong(lastQueueNumberStr) : 0;
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드 (todo - 업데이트는 payment 에서)
  public Long getLastExitedQueueNumber() {
    String lastQueueNumberStr = redisTemplate.opsForValue().get(LAST_EXITED_QUEUE_NUMBER_KEY);
    return lastQueueNumberStr != null ? Long.parseLong(lastQueueNumberStr) : 0;
  }

  public QueuePolling checkQueueStatus(QueuePollingCommand queuePollingCommand) {
    jwtUtil.validateToken(
        queuePollingCommand.jwtToken()); // TODO - 토큰서비스에서 간접적으로 사용할지, 직접 사용할지 고려할 것

    Long lastExitedQueueNumber = getLastExitedQueueNumber();
    Long requestQueueNumber = queuePollingCommand.queueNumber();
    long remainingInQueue = requestQueueNumber - lastExitedQueueNumber;

    if (remainingInQueue <= 0) {
      return new QueuePolling(true, 0L, null);
    } else {
      String newToken = jwtUtil.extendQueueJwtExpiration(queuePollingCommand.jwtToken(),
          3600 * 1000L);
      return new QueuePolling(false, remainingInQueue, newToken);
    }
  }
}
