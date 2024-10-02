package com.quickdeal.auth.inbound.api.controller;

import com.quickdeal.auth.inbound.api.resource.LoginResource;
import com.quickdeal.auth.service.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

  private final Logger log;
  private final LoginService loginService;

  public AuthController(LoginService loginService) {
    this.log = LoggerFactory.getLogger(this.getClass());
    this.loginService = loginService;
  }

  @PostMapping("/login")
  public LoginResource login() {
    log.debug("<controller> [login] start login");
    return new LoginResource(loginService.getUserId());
  }
}
