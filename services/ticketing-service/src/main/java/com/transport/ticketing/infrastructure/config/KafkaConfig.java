package com.transport.ticketing.infrastructure.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Configuration
 * Creates topics automatically if they don't exist
 */
@Configuration
public class KafkaConfig {
    
    public static final String TICKET_PURCHASED_TOPIC = "ticket.purchased";
    public static final String PAYMENT_COMPLETED_TOPIC = "payment.completed";
    public static final String PAYMENT_FAILED_TOPIC = "payment.failed";
    
    @Bean
    public NewTopic ticketPurchasedTopic() {
        return TopicBuilder.name(TICKET_PURCHASED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic paymentCompletedTopic() {
        return TopicBuilder.name(PAYMENT_COMPLETED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
    
    @Bean
    public NewTopic paymentFailedTopic() {
        return TopicBuilder.name(PAYMENT_FAILED_TOPIC)
                .partitions(3)
                .replicas(1)
                .build();
    }
}