package com.ecoshop.support.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication(scanBasePackages = {"com.ecoshop.support.service", "com.ecoshop.common"})
@EnableDiscoveryClient
@EnableFeignClients
@EnableJpaAuditing
@EnableKafka
public class SupportServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupportServiceApplication.class, args);
    }
}
