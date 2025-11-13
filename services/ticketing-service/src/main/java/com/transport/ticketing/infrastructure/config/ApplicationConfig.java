package com.transport.ticketing.infrastructure.config;

import com.transport.ticketing.domain.service.PricingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for domain services
 * Register domain objects as Spring beans
 */
@Configuration
public class ApplicationConfig {
    
    /**
     * PricingService is a domain service with no dependencies
     * We create it as a bean so Spring can inject it into Use Cases
     */
    @Bean
    public PricingService pricingService() {
        return new PricingService();
    }
}