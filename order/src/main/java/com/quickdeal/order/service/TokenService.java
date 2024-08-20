package com.quickdeal.order.service;

import com.quickdeal.order.domain.QueueToken;
import com.quickdeal.order.util.JWTUtil;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final JWTUtil jwtUtil;

  public TokenService(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  // 대기열 토큰 생성 로직
  public QueueToken generateQueueNumber(Long productId, String userId, Long newQueueNumber) {
    String token = jwtUtil.createQueueJwt(userId, newQueueNumber);
    return new QueueToken(newQueueNumber, productId, userId, token);
  }
}
