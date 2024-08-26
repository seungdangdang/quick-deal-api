package com.quickdeal.order.service;

import com.quickdeal.common.exception.JWTTokenException;
import com.quickdeal.order.domain.QueueToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final Key hmacKey;

  public TokenService(@Value("${jwt.secret-key}") String secretKey) {
    this.hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secretKey),
        SignatureAlgorithm.HS256.getJcaName());
  }

  public QueueToken generateQueueNumber(Long productId, String userId, Long newQueueNumber) {
    Date now = new Date();
    String token = Jwts.builder()
        .setHeaderParam("type", "jwt")
        .claim("user_id", userId)
        .claim("queue_number", newQueueNumber)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + 1000L * 60L * 60L * 2L))
        .signWith(hmacKey)
        .compact();

    return new QueueToken(newQueueNumber, productId, userId, token);
  }

  public String extendQueueJwtExpiration(String token, Long extensionInMillis) {
    Claims claims = Jwts.parserBuilder()
        .setSigningKey(hmacKey)
        .build()
        .parseClaimsJws(token)
        .getBody();

    Date now = new Date();
    Date newExpirationDate = new Date(now.getTime() + extensionInMillis);

    return Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(now)
        .setExpiration(newExpirationDate)
        .signWith(hmacKey, SignatureAlgorithm.HS256)
        .compact();
  }

  public Claims validateTokenAndGetClaims(String token) {
    try {
      return Jwts.parserBuilder()
          .setSigningKey(hmacKey)
          .build()
          .parseClaimsJws(token)
          .getBody();
    } catch (MalformedJwtException e) {
      throw new JWTTokenException("잘못된 JWT 토큰입니다 : " + e.getMessage());
    } catch (ExpiredJwtException e) {
      throw new JWTTokenException("만료된 JWT 토큰입니다 : " + e.getMessage());
    } catch (UnsupportedJwtException e) {
      throw new JWTTokenException("지원되지 않는 JWT 토큰입니다 : " + e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new JWTTokenException("JWT 토큰이 유효하지 않습니다 : " + e.getMessage());
    }
  }
}
