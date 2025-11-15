package com.transport.ticketing.infrastructure.event;

import com.transport.ticketing.domain.event.TicketPurchasedEvent;
import com.transport.ticketing.infrastructure.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka Event Publisher
 * Publishes domain events to Kafka topics
 */
@Component
public class KafkaEventPublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(KafkaEventPublisher.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    public KafkaEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publish TicketPurchasedEvent to Kafka
     */
    public void publishTicketPurchased(TicketPurchasedEvent event) {
        logger.info("Publishing TicketPurchasedEvent: orderId={}, amount={}", 
                   event.getOrderId(), event.getAmount());
        
        try {
            // Send to Kafka
            CompletableFuture<SendResult<String, Object>> future = 
                kafkaTemplate.send(
                    KafkaConfig.TICKET_PURCHASED_TOPIC, 
                    event.getOrderId().toString(), // Use orderId as key for partitioning
                    event
                );
            
            // Add callback to log success/failure
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ Event published successfully: topic={}, partition={}, offset={}", 
                               result.getRecordMetadata().topic(),
                               result.getRecordMetadata().partition(),
                               result.getRecordMetadata().offset());
                } else {
                    logger.error("❌ Failed to publish event: {}", ex.getMessage(), ex);
                }
            });
            
        } catch (Exception e) {
            logger.error("Error publishing TicketPurchasedEvent", e);
            // In production, you might want to:
            // 1. Retry with exponential backoff
            // 2. Store in dead letter queue
            // 3. Send alert to monitoring system
        }
    }
}