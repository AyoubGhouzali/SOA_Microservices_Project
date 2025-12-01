package com.transport.payment.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
}