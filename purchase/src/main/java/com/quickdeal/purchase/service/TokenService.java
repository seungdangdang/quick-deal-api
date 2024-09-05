package com.quickdeal.purchase.service;

import com.quickdeal.common.exception.JWTTokenException;
import com.quickdeal.purchase.domain.Ticket;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import java.security.Key;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

  private final Key hmacKey;
  private final Duration expiration;

  public TokenService(
      @Value("${jwt.secret-key}") String secretKey,
      @Value("${ticket-token.expiration}") Duration expiration
  ) {
    this.hmacKey = new SecretKeySpec(Base64.getDecoder().decode(secretKey),
        SignatureAlgorithm.HS256.getJcaName());
    this.expiration = expiration;
  }

  public Ticket generateTicketNumber(Long productId, String userId, Long ticketNumber,
      Long orderId) {
    Date now = new Date();
    String token = Jwts.builder().setHeaderParam("type", "jwt").claim("product_id", productId)
        .claim("user_id", userId)
        .claim("ticket_number", ticketNumber)
        .claim("order_id", orderId)
        .setIssuedAt(now).setExpiration(new Date(now.getTime() + expiration.toMillis()))
        .signWith(hmacKey).compact();

    return new Ticket(orderId, token);
  }

  public String extendTicketJwtExpiration(String token, Long extensionInMillis) {
    Claims claims = Jwts.parserBuilder().setSigningKey(hmacKey).build().parseClaimsJws(token)
        .getBody();

    Date now = new Date();
    Date newExpirationDate = new Date(now.getTime() + extensionInMillis);

    return Jwts.builder().setClaims(claims).setIssuedAt(now).setExpiration(newExpirationDate)
        .signWith(hmacKey, SignatureAlgorithm.HS256).compact();
  }

  public Claims validateTokenAndGetClaims(String token) {
    try {
      return Jwts.parserBuilder().setSigningKey(hmacKey).build().parseClaimsJws(token).getBody();
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
