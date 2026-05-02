package com.ecoshop.user.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.ecoshop.user.service", "com.ecoshop.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
