package com.quickdeal.purchase.outbound.redis.repository;

import org.springframework.data.redis.core.script.RedisScript;

class OrderPaymentSlotConstants {

  private static final String EXISTS_PAYMENT_SLOT_LUA_SCRIPT_STRING = """
      local productId = KEYS[1]
      local ticketNumber = tonumber(ARGV[1])
      local userId = ARGV[2]
      local maxPaymentPageUsers = tonumber(ARGV[3])
      local expiredAtEpochSeconds = tonumber(ARGV[4])
      
      -- 변수 값들을 문자열로 변환하여 로그 출력
      redis.log(redis.LOG_NOTICE, string.format("[User ID: %s] Product ID: %s, Ticket Number: %d, Max Payment Users: %d, Expiry Time: %d",
              tostring(userId), tostring(productId), ticketNumber, maxPaymentPageUsers, expiredAtEpochSeconds))
      
      -- 키 변수화
      local paymentAccessibleUserIdZsetKey = "product:" .. productId .. ":paymentPageUser"
      local lastExitedTicketNumberKey = "product:" .. productId .. ":lastExitedTicketNumber"
      
      -- 현재 접속자 수 확인 (Sorted Set의 개수를 ZCARD로 가져옴)
      local currentAccessCount = redis.call("ZCARD", paymentAccessibleUserIdZsetKey)
      redis.log(redis.LOG_NOTICE, "[" .. userId .. "][redis.call(\\"ZCARD\\", paymentAccessibleUserIdZsetKey)] 현재 접속자 수 확인 (Sorted Set의 개수를 ZCARD로 가져옴) 결과 " .. tostring(currentAccessCount) .. "/" .. tostring(maxPaymentPageUsers) .. "명 접속중입니다.")
      
      -- 사용자 ID가 정렬된 집합(Sorted Set)에 존재하는지 확인
      local isUserPresent = redis.call("ZSCORE", paymentAccessibleUserIdZsetKey, userId)
      redis.log(redis.LOG_NOTICE, "[" .. userId .. "][redis.call(\\"ZSCORE\\", paymentAccessibleUserIdZsetKey, userId)] 사용자 ID가 정렬된 집합(Sorted Set)에 존재하는지 확인 결과 " .. "isUserPresent: " .. tostring(isUserPresent) .. ", ZSCORE product:5:lastTicketNumber " .. userId)
      
      if currentAccessCount < maxPaymentPageUsers and not isUserPresent then
          -- 사용자 추가 및 만료 시간 설정
          redis.call("ZADD", paymentAccessibleUserIdZsetKey, expiredAtEpochSeconds, userId)
          redis.log(redis.LOG_NOTICE, "[" .. userId .. "][redis.call(\\"ZADD\\", paymentAccessibleUserIdZsetKey, expiredAtEpochSeconds, userId)] " .. "ZADD paymentAccessibleUserIdZsetKey:" .. tostring(paymentAccessibleUserIdZsetKey))
      
          local previousTicketNumber = redis.call("GET", lastExitedTicketNumberKey)
          redis.log(redis.LOG_NOTICE, "[" .. userId .. "][redis.call(\\"GET\\", lastExitedTicketNumberKey)] " .. "previousTicketNumber: " .. tostring(previousTicketNumber))
          local status, err = pcall(function()
              -- 마지막 처리 대기열 번호 업데이트
              redis.call("SET", lastExitedTicketNumberKey, ticketNumber)
              redis.log(redis.LOG_NOTICE, "[" .. userId .. "][redis.call(\\"SET\\", lastExitedTicketNumberKey, ticketNumber)] " .. "updatedTicketNumber: " .. tostring(ticketNumber))
          end)
      
          if not status then
              redis.log(redis.LOG_WARNING, "[" .. userId .. "][redis.call(\\"SET\\", lastExitedTicketNumberKey, ticketNumber)] " .. "pcall 오류 발생: " .. tostring(err))
      
              local rollbackStatus, rollbackErr = pcall(function()
                  redis.call("ZREM", paymentAccessibleUserIdZsetKey, userId)
              end)
              if not rollbackStatus then
                  redis.log(redis.LOG_WARNING, "[\\" .. userId .. \\"][redis.call(\\"ZREM\\", paymentAccessibleUserIdZsetKey, userId)] 접속자 수 롤백 중 오류 발생: " .. tostring(rollbackErr))
              end
      
              local queueRollbackStatus, queueRollbackErr = pcall(function()
                  if previousTicketNumber then
                      redis.call("SET", lastExitedTicketNumberKey, previousTicketNumber)
                  else
                      redis.call("DEL", lastExitedTicketNumberKey)
                  end
              end)
              if not queueRollbackStatus then
                  redis.log(redis.LOG_WARNING, "[" .. userId .. "][redis.call(\\"SET\\", lastExitedTicketNumberKey, previousTicketNumber) / redis.call(\\"DEL\\", lastExitedTicketNumberKey)] " .. "대기열 번호 롤백 중 오류 발생: " .. tostring(queueRollbackErr))
              end
      
              return redis.error_reply("[\\" .. userId .. \\"] 작업 중 오류가 발생하여 롤백되었습니다.")
          end
      
          return 1
      else
          return 0
      end
      """;

  public static final RedisScript<Long> EXISTS_PAYMENT_SLOT_LUA_SCRIPT =
      RedisScript.of(EXISTS_PAYMENT_SLOT_LUA_SCRIPT_STRING, Long.class);

  private static final String GET_USER_EXPIRATION_TIME_LUA_SCRIPT_STRING = """
      local key = KEYS[1]
      local userId = ARGV[1]
      local isUserPresent = redis.call('ZSCORE', key, userId)
      
      if isUserPresent then
          -- ZSCORE 명령어가 사용자 ID를 찾았을 때
          redis.log(redis.LOG_WARNING, string.format("[INFO] User found in sorted set. User ID: %s, Key: %s, Score: %s", userId, key, tostring(isUserPresent)))
          return tonumber(isUserPresent)
      else
          -- ZSCORE 명령어가 사용자 ID를 찾지 못했을 때
          redis.log(redis.LOG_WARNING, string.format("[INFO] User not found in sorted set. User ID: %s, Key: %s, Result: nil", userId, key))
          return nil
      end
      """;

  public static final RedisScript<Long> GET_USER_EXPIRATION_TIME_LUA_SCRIPT =
      RedisScript.of(GET_USER_EXPIRATION_TIME_LUA_SCRIPT_STRING, Long.class);
}
