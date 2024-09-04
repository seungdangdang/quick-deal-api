local productId = KEYS[1]
local ticketNumber = tonumber(ARGV[1])
local userId = ARGV[2]
local maxPaymentPageUsers = tonumber(ARGV[3])

-- 키 변수화
local paymentPageUserCountKey = "product:" .. productId .. ":paymentPageUser"
local lastExitedQueueNumberKey = "product:" .. productId .. ":lastExitedQueueNumber"

-- 현재 접속자 수 확인
local currentAccessCount = redis.call("SCARD", paymentPageUserCountKey)
redis.log(redis.LOG_NOTICE, tostring(currentAccessCount) .. "명 접속중입니다.")

-- UUID 가 있는지 검토
local isUserPresent = redis.call("SISMEMBER", paymentPageUserCountKey, userId)

if currentAccessCount < maxPaymentPageUsers and isUserPresent == 0 then
    redis.call("SADD", paymentPageUserCountKey, userId)

    local previousQueueNumber = redis.call("GET", lastExitedQueueNumberKey)
    local status, err = pcall(function()
        -- 마지막 처리 대기열 번호 업데이트
        redis.log(redis.LOG_NOTICE, "업데이트된 티켓 넘버 " .. tostring(ticketNumber))
        redis.call("SET", lastExitedQueueNumberKey, ticketNumber)
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
            if previousQueueNumber then
                redis.call("SET", lastExitedQueueNumberKey, previousQueueNumber)
            else
                redis.call("DEL", lastExitedQueueNumberKey)
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
