package com.transport.payment.service;

import com.transport.payment.event.PaymentProcessedEvent;
import com.transport.payment.event.TicketPurchasedEvent;
import com.transport.payment.model.Payment;
import com.transport.payment.model.PaymentMethod;
import com.transport.payment.model.PaymentStatus;
import com.transport.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Payment Service - Handles payment processing logic
 */
@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private static final String PAYMENT_PROCESSED_TOPIC = "payment.processed";
    private static final Random random = new Random();

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentService(PaymentRepository paymentRepository,
                         KafkaTemplate<String, Object> kafkaTemplate) {
        this.paymentRepository = paymentRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Process payment for ticket purchase
     * This is a SIMULATED payment - no real payment gateway integration
     */
    @Transactional
    public Payment processTicketPayment(TicketPurchasedEvent event) {
        logger.info("💳 Processing payment for order: {}", event.getOrderId());

        // 1. Create payment record
        Payment payment = createPaymentRecord(event);

        // 2. Mark as processing
        payment.markAsProcessing();
        paymentRepository.save(payment);

        // 3. Simulate payment processing
        boolean success = simulatePaymentProcessing(payment);

        // 4. Update payment status
        if (success) {
            payment.markAsCompleted();
            logger.info("✅ Payment COMPLETED - Transaction ID: {}", payment.getTransactionId());
        } else {
            payment.markAsFailed("Simulated payment failure");
            logger.error("❌ Payment FAILED for order: {}", event.getOrderId());
        }

        payment = paymentRepository.save(payment);

        // 5. Publish PaymentProcessedEvent to Kafka
        publishPaymentProcessedEvent(payment);

        return payment;
    }

    /**
     * Create initial payment record from ticket purchase event
     */
    private Payment createPaymentRecord(TicketPurchasedEvent event) {
        Payment payment = new Payment();
        payment.setOrderId(event.getOrderId());
        payment.setUserId(event.getUserId());
        payment.setTicketId(event.getTicketId());
        payment.setAmount(event.getAmount());
        payment.setCurrency(event.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD); // Default for simulation
        payment.setPaymentType("TICKET_PURCHASE");
        payment.setDescription("Ticket purchase - " + event.getTicketType());

        return paymentRepository.save(payment);
    }

    /**
     * Simulate payment processing with a payment gateway
     * In production, this would call Stripe/PayPal API
     *
     * Simulation: 95% success rate, 500ms processing time
     */
    private boolean simulatePaymentProcessing(Payment payment) {
        logger.info("🔄 Simulating payment gateway processing...");
        logger.info("   Amount: {} {}", payment.getAmount(), payment.getCurrency());
        logger.info("   Method: {}", payment.getPaymentMethod());

        try {
            // Simulate network delay
            Thread.sleep(500);

            // Simulate 95% success rate
            boolean success = random.nextInt(100) < 95;

            if (success) {
                logger.info("💰 Payment gateway: APPROVED");
            } else {
                logger.warn("⚠️  Payment gateway: DECLINED");
            }

            return success;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Payment processing interrupted", e);
            return false;
        }
    }

    /**
     * Publish PaymentProcessedEvent to Kafka
     * Other services (ticketing, subscription) can listen to this
     */
    private void publishPaymentProcessedEvent(Payment payment) {
        PaymentProcessedEvent event = new PaymentProcessedEvent(
            payment.getId(),
            payment.getOrderId(),
            payment.getUserId(),
            payment.getTransactionId(),
            payment.getAmount(),
            payment.getCurrency(),
            payment.getStatus().name(),
            payment.getPaymentType()
        );

        if (payment.getStatus() == PaymentStatus.FAILED) {
            event.setFailureReason(payment.getFailureReason());
        }

        kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC, event.getOrderId().toString(), event);
        logger.info("📤 Published PaymentProcessedEvent to Kafka topic: {}", PAYMENT_PROCESSED_TOPIC);
        logger.info("   Event ID: {}", event.getEventId());
        logger.info("   Transaction ID: {}", event.getTransactionId());
        logger.info("   Status: {}", event.getStatus());
    }

    /**
     * Get payment by order ID
     */
    public Payment getPaymentByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Payment not found for order: " + orderId));
    }

    /**
     * Get all payments for a user
     */
    public List<Payment> getUserPayments(UUID userId) {
        return paymentRepository.findByUserId(userId);
    }

    /**
     * Get payment by transaction ID
     */
    public Payment getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId)
            .orElseThrow(() -> new RuntimeException("Payment not found for transaction: " + transactionId));
    }

    /**
     * Get payment statistics
     */
    public PaymentStats getPaymentStats() {
        long totalPayments = paymentRepository.count();
        long completedPayments = paymentRepository.findByStatus(PaymentStatus.COMPLETED).size();
        long failedPayments = paymentRepository.findByStatus(PaymentStatus.FAILED).size();
        long pendingPayments = paymentRepository.findByStatus(PaymentStatus.PENDING).size();

        BigDecimal totalRevenue = paymentRepository.findByStatus(PaymentStatus.COMPLETED).stream()
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PaymentStats(totalPayments, completedPayments, failedPayments,
                               pendingPayments, totalRevenue);
    }

    /**
     * Inner class for payment statistics
     */
    public static class PaymentStats {
        private final long totalPayments;
        private final long completedPayments;
        private final long failedPayments;
        private final long pendingPayments;
        private final BigDecimal totalRevenue;

        public PaymentStats(long totalPayments, long completedPayments, long failedPayments,
                           long pendingPayments, BigDecimal totalRevenue) {
            this.totalPayments = totalPayments;
            this.completedPayments = completedPayments;
            this.failedPayments = failedPayments;
            this.pendingPayments = pendingPayments;
            this.totalRevenue = totalRevenue;
        }

        public long getTotalPayments() { return totalPayments; }
        public long getCompletedPayments() { return completedPayments; }
        public long getFailedPayments() { return failedPayments; }
        public long getPendingPayments() { return pendingPayments; }
        public BigDecimal getTotalRevenue() { return totalRevenue; }
    }
}
