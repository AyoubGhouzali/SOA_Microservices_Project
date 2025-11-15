package com.transport.ticketing_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.transport.ticketing")
@EnableJpaRepositories(basePackages = "com.transport.ticketing.infrastructure.persistence")
@EntityScan(basePackages = "com.transport.ticketing.infrastructure.persistence")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}