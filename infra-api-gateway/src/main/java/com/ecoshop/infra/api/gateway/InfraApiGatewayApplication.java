package com.ecoshop.infra.api.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InfraApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfraApiGatewayApplication.class, args);
    }
}
