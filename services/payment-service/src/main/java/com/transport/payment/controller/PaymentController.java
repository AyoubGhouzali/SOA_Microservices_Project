package com.transport.payment.controller;

import com.transport.payment.model.Payment;
import com.transport.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST Controller for Payment operations
 *
 * Base URL: /api/payments
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Health check endpoint
     *
     * GET /api/payments/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Payment Service is UP");
    }

    /**
     * Get payment service status
     *
     * GET /api/payments/status
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "payment-service");
        status.put("status", "running");
        status.put("version", "1.0.0");
        status.put("kafka", "connected");

        logger.info("GET /api/payments/status");
        return ResponseEntity.ok(status);
    }

    /**
     * Get payment by order ID
     *
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<Payment> getPaymentByOrderId(@PathVariable UUID orderId) {
        logger.info("GET /api/payments/order/{}", orderId);
        Payment payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get payment by transaction ID
     *
     * GET /api/payments/transaction/{transactionId}
     */
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<Payment> getPaymentByTransactionId(@PathVariable String transactionId) {
        logger.info("GET /api/payments/transaction/{}", transactionId);
        Payment payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get all payments for a user
     *
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getUserPayments(@PathVariable UUID userId) {
        logger.info("GET /api/payments/user/{}", userId);
        List<Payment> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get payment statistics
     *
     * GET /api/payments/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<PaymentService.PaymentStats> getPaymentStats() {
        logger.info("GET /api/payments/stats");
        PaymentService.PaymentStats stats = paymentService.getPaymentStats();
        return ResponseEntity.ok(stats);
    }
}