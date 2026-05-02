package com.ecoshop.infra.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class InfraConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfraConfigServerApplication.class, args);
    }
}
