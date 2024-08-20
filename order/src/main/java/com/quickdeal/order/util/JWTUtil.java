package com.quickdeal.order.util;

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
import org.springframework.stereotype.Component;

@Component
public class JWTUtil {

  private final Key hmacKey;

  public JWTUtil(@Value("${jwt.secret-key}") String secretKey) {
    this.hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secretKey),
        SignatureAlgorithm.HS256.getJcaName());
  }

  public String createQueueJwt(String userId, Long queueNumber) {
    Date now = new Date();

    return Jwts.builder()
        .setHeaderParam("type", "jwt")
        .claim("user_id", userId)
        .claim("queue_number", queueNumber)
        .setIssuedAt(now)
        .setExpiration(new Date(now.getTime() + 1000L * 60L * 60L * 24L * 365L))
        .signWith(hmacKey)
        .compact();
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

  public void validateToken(String token) {
    try {
      Jwts.parserBuilder()
          .setSigningKey(hmacKey)
          .build()
          .parseClaimsJws(token)
          .getBody();

    } catch (MalformedJwtException e) {
      throw new IllegalArgumentException("잘못된 JWT 토큰입니다 : " + e.getMessage());
    } catch (ExpiredJwtException e) {
      throw new IllegalArgumentException("만료된 JWT 토큰입니다 : " + e.getMessage());
    } catch (UnsupportedJwtException e) {
      throw new IllegalArgumentException("지원되지 않는 JWT 토큰입니다 : " + e.getMessage());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("JWT 토큰이 유효하지 않습니다 : " + e.getMessage());
    }
  }
}
