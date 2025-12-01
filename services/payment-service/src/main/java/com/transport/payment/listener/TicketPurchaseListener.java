package com.transport.payment.listener;

import com.transport.payment.event.TicketPurchasedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka Consumer - Listens for ticket purchase events
 */
@Component
public class TicketPurchaseListener {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketPurchaseListener.class);
    
    @KafkaListener(
        topics = "ticket.purchased",
        groupId = "payment-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleTicketPurchased(
            @Payload TicketPurchasedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {
        
        logger.info("📨 Received TicketPurchasedEvent from Kafka:");
        logger.info("   - Order ID: {}", event.getOrderId());
        logger.info("   - Ticket ID: {}", event.getTicketId());
        logger.info("   - User ID: {}", event.getUserId());
        logger.info("   - Amount: {} {}", event.getAmount(), event.getCurrency());
        logger.info("   - Type: {}", event.getTicketType());
        logger.info("   - Partition: {}, Offset: {}", partition, offset);
        
        try {
            // TODO: Process payment here
            // For now, just log that we received it
            processPayment(event);
            
            logger.info("✅ Payment processed successfully for order: {}", event.getOrderId());
            
        } catch (Exception e) {
            logger.error("❌ Failed to process payment for order: {}", 
                        event.getOrderId(), e);
            // In production: retry logic, dead letter queue, etc.
        }
    }
    
    /**
     * Simulate payment processing
     * In production, this would integrate with Stripe/PayPal
     */
    private void processPayment(TicketPurchasedEvent event) {
        logger.info("💳 Processing payment...");
        
        // Simulate payment delay
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        logger.info("💰 Payment of {} {} charged successfully", 
                   event.getAmount(), event.getCurrency());
    }
}