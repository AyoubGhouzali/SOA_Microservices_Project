package com.transport.payment.model;

/**
 * Payment Status Enum
 */
public enum PaymentStatus {
    PENDING,      // Payment initiated but not processed
    PROCESSING,   // Payment is being processed
    COMPLETED,    // Payment successful
    FAILED,       // Payment failed
    REFUNDED,     // Payment was refunded
    CANCELLED     // Payment was cancelled
}
