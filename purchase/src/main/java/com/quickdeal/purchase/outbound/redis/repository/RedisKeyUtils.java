package com.quickdeal.purchase.outbound.redis.repository;

class RedisKeyUtils {

  public static String getLastTicketNumberKey(Long productId) {
    return "product:" + productId + ":lastTicketNumber";
  }

  public static String getLastExitedTicketNumberKey(Long productId) {
    return "product:" + productId + ":lastExitedTicketNumber";
  }

  public static String getPaymentPageUserKey(Long productId) {
    return "product:" + productId + ":paymentPageUser";
  }
}
