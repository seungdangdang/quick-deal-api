package com.quickdeal.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.quickdeal.product.infrastructure.repository")
@EntityScan(basePackages = "com.quickdeal.product.infrastructure.entity")
public class JpaConfig {
}
