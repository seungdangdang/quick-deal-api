package com.quickdeal.auth.service;

import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

  public String getUserId() {
    return UUID.randomUUID().toString();
  }
}
