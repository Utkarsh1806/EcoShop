package com.ecoshop.cart.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.ecoshop.cart.service", "com.ecoshop.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableKafka
public class CartServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CartServiceApplication.class, args);
    }
}
