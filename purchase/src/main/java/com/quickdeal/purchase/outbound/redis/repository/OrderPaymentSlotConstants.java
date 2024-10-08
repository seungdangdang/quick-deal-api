package com.quickdeal.purchase.outbound.redis.repository;

import org.springframework.data.redis.core.script.RedisScript;

class OrderPaymentSlotConstants {

  private static final String UPDATE_LAST_PROCESSED_TICKET_AND_ADD_PAYMENT_USERS_STRING = """
      -- (로그 기록) 입력된 사용자 수
      local numberOfUsers = (#ARGV - 1) / 2
      redis.log(redis.LOG_NOTICE, "Processing " .. numberOfUsers .. " users for payment access.")
      
      local paymentPageAccessUserKey = KEYS[1]
      local lastExitedTicketNumberKey = KEYS[2]
      local expiredEpochSeconds = ARGV[1]
      local rollbackRequired = false
      
      -- 사용자 ID와 만료 시간을 Sorted Set에 일괄 추가
      local zaddArgs = {paymentPageAccessUserKey}
      for i = 2, #ARGV, 2 do
          local userId = ARGV[i]
          local ticketNumber = ARGV[i + 1]
          table.insert(zaddArgs, expiredEpochSeconds)
          table.insert(zaddArgs, userId)
      end
      
      -- ZADD 명령어를 한 번의 호출로 실행
      local status, err = pcall(function()
          redis.call('ZADD', unpack(zaddArgs))
      end)
      
      if not status then
          rollbackRequired = true
          redis.log(redis.LOG_WARNING, "Error adding users to ZSET: " .. tostring(err))
      end
      
      if rollbackRequired then
          -- ZADD 작업 중 오류가 발생한 경우 롤백 수행
          for i = 2, #ARGV, 2 do
              local userId = ARGV[i]
              redis.call('ZREM', paymentPageAccessUserKey, userId)
          end
          redis.log(redis.LOG_WARNING, "Rollback: removed added users from ZSET.")
          return redis.error_reply("An error occurred while adding users. Rollback completed.")
      end
      
      -- 마지막 ticketNumber를 찾기 위해 ARGV 리스트의 maximum 추출
      local maxTicketNumber = -1
      for i = 3, #ARGV, 2 do
          local ticketNumber = tonumber(ARGV[i])
          if ticketNumber > maxTicketNumber then
              maxTicketNumber = ticketNumber
          end
      end
      
      -- lastExitedTicketNumberKey에 가장 큰 ticketNumber를 설정
      local previousTicketNumber = redis.call('GET', lastExitedTicketNumberKey)
      local status, err = pcall(function()
          redis.call('SET', lastExitedTicketNumberKey, maxTicketNumber)
      end)
      
      if not status then
          -- SET 작업 중 오류가 발생한 경우 롤백 수행
          redis.log(redis.LOG_WARNING, "Error setting last exited ticket number: " .. tostring(err))
          if previousTicketNumber then
              redis.call('SET', lastExitedTicketNumberKey, previousTicketNumber)
          else
              redis.call('DEL', lastExitedTicketNumberKey)
          end
          redis.log(redis.LOG_WARNING, "Rollback: restored previous ticket number.")
          return redis.error_reply("An error occurred while setting the last exited ticket number. Rollback completed.")
      end
      
      return 1
      """;

  public static final RedisScript<Long> UPDATE_LAST_PROCESSED_TICKET_AND_ADD_PAYMENT_USERS =
      RedisScript.of(UPDATE_LAST_PROCESSED_TICKET_AND_ADD_PAYMENT_USERS_STRING, Long.class);

}
