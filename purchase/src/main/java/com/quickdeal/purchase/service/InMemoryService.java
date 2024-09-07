package com.quickdeal.purchase.service;

import com.quickdeal.purchase.config.RedisConfig;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Service
public class InMemoryService {

  private final Logger log;
  private final RedisTemplate<String, Long> longValueRedisTemplate;
  private final RedisTemplate<String, String> stringValueRedisTemplate;
  private final Jedis jedis;

  public InMemoryService(RedisTemplate<String, Long> redisTemplate,
      RedisTemplate<String, String> stringValueRedisTemplate, Jedis jedis) {
    this.longValueRedisTemplate = redisTemplate;
    this.stringValueRedisTemplate = stringValueRedisTemplate;
    this.jedis = jedis;
    this.log = LoggerFactory.getLogger(InMemoryService.class);
  }

  public boolean validatePaymentPageAccess(List<String> keys, List<String> args) {
    String luaScript = """
        local productId = KEYS[1]
        local ticketNumber = tonumber(ARGV[1])
        local userId = ARGV[2]
        local maxPaymentPageUsers = tonumber(ARGV[3])
        
        -- 키 변수화
        local paymentPageUserCountKey = "product:" .. productId .. ":paymentPageUser"
        local lastExitedTicketNumberKey = "product:" .. productId .. ":lastExitedTicketNumber"
        
        -- 현재 접속자 수 확인
        local currentAccessCount = redis.call("SCARD", paymentPageUserCountKey)
        redis.log(redis.LOG_NOTICE, tostring(currentAccessCount) .. "명 접속중입니다.")
        
        -- UUID 가 있는지 검토
        local isUserPresent = redis.call("SISMEMBER", paymentPageUserCountKey, userId)
        
        if currentAccessCount < maxPaymentPageUsers and isUserPresent == 0 then
            redis.call("SADD", paymentPageUserCountKey, userId)
        
            local previousTicketNumber = redis.call("GET", lastExitedTicketNumberKey)
            local status, err = pcall(function()
                -- 마지막 처리 대기열 번호 업데이트
                redis.log(redis.LOG_NOTICE, "업데이트된 티켓 넘버 " .. tostring(ticketNumber))
                redis.call("SET", lastExitedTicketNumberKey, ticketNumber)
            end)
        
            if not status then
                redis.log(redis.LOG_WARNING, "pcall 오류 발생: " .. err)
        
                local rollbackStatus, rollbackErr = pcall(function()
                    redis.call("SREM", paymentPageUserCountKey, userId)
                end)
                if not rollbackStatus then
                    redis.log(redis.LOG_WARNING, "접속자 수 롤백 중 오류 발생: " .. rollbackErr)
                end
        
                local queueRollbackStatus, queueRollbackErr = pcall(function()
                    if previousTicketNumber then
                        redis.call("SET", lastExitedTicketNumberKey, previousTicketNumber)
                    else
                        redis.call("DEL", lastExitedTicketNumberKey)
                    end
                end)
                if not queueRollbackStatus then
                    redis.log(redis.LOG_WARNING, "대기열 번호 롤백 중 오류 발생: " .. queueRollbackErr)
                end
        
                return redis.error_reply("작업 중 오류가 발생하여 롤백되었습니다.")
            end
        
            return 1
        else
            return 0
        end
        """;

    Object result = executeLuaScript(luaScript, keys, args);
    return Integer.parseInt(result.toString()) == 1;
  }

  private Object executeLuaScript(String script, List<String> keys, List<String> args) {
    return jedis.eval(script, keys, args);
  }

  public Long getNewTicketNumber(Long productId) {
    return incrementLastTicketNumber(productId);
  }

  private Long incrementLastTicketNumber(Long productId) {
    String key = RedisConfig.getLastTicketNumberKey(productId);
    Object value = longValueRedisTemplate.opsForValue().increment(key, 1);
    return value != null ? Long.parseLong(String.valueOf(value)) : 1L;
  }

  public void decrementLastTicketNumber(Long productId) {
    String key = RedisConfig.getLastTicketNumberKey(productId);
    longValueRedisTemplate.opsForValue().increment(key, -1);
  }

  // 레디스에서 마지막으로 대기열에서 빠져나간 대기번호 가져오는 코드
  public Long getLastExitedTicketNumber(Long productId) {
    String key = RedisConfig.getLastExitedTicketNumberKey(productId);
    Object value = longValueRedisTemplate.opsForValue().get(key);
    return value != null ? Long.parseLong(String.valueOf(value)) : 0L;
  }

  public void updateLastExitedTicketNumber(Long productId, Long lastExitedTicketNumber) {
    String key = RedisConfig.getLastExitedTicketNumberKey(productId);
    longValueRedisTemplate.opsForValue().set(key, lastExitedTicketNumber);
  }

  public void removePaymentPageUser(Long productId, String userId) {
    String key = RedisConfig.getPaymentPageUserKey(productId);
    stringValueRedisTemplate.opsForSet().remove(key, userId);
  }
}
