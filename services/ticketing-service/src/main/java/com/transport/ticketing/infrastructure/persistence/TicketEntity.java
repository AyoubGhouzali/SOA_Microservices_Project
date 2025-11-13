package com.transport.ticketing.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JPA Entity - Infrastructure concern
 * Maps to database table
 * 
 * NOTE: This is NOT the domain model!
 * This is a database representation
 */
@Entity
@Table(name = "tickets", indexes = {
    @Index(name = "idx_tickets_user_id", columnList = "user_id"),
    @Index(name = "idx_tickets_order_id", columnList = "order_id"),
    @Index(name = "idx_tickets_status", columnList = "status"),
    @Index(name = "idx_tickets_qr_code", columnList = "qr_code", unique = true)
})
public class TicketEntity {
    
    @Id
    @Column(name = "ticket_id")
    private UUID id;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    
    @Column(name = "ticket_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TicketTypeEntity type;
    
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TicketStatusEntity status;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Column(name = "validity_start")
    private LocalDateTime validityStart;
    
    @Column(name = "validity_end")
    private LocalDateTime validityEnd;
    
    @Column(name = "remaining_validations", nullable = false)
    private int remainingValidations;
    
    @Column(name = "purchased_at", nullable = false)
    private LocalDateTime purchasedAt;
    
    @Column(name = "activated_at")
    private LocalDateTime activatedAt;
    
    @Column(name = "qr_code", length = 500)
    private String qrCode;
    
    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public TicketTypeEntity getType() { return type; }
    public void setType(TicketTypeEntity type) { this.type = type; }
    
    public TicketStatusEntity getStatus() { return status; }
    public void setStatus(TicketStatusEntity status) { this.status = status; }
    
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public LocalDateTime getValidityStart() { return validityStart; }
    public void setValidityStart(LocalDateTime validityStart) { 
        this.validityStart = validityStart; 
    }
    
    public LocalDateTime getValidityEnd() { return validityEnd; }
    public void setValidityEnd(LocalDateTime validityEnd) { 
        this.validityEnd = validityEnd; 
    }
    
    public int getRemainingValidations() { return remainingValidations; }
    public void setRemainingValidations(int remainingValidations) { 
        this.remainingValidations = remainingValidations; 
    }
    
    public LocalDateTime getPurchasedAt() { return purchasedAt; }
    public void setPurchasedAt(LocalDateTime purchasedAt) { 
        this.purchasedAt = purchasedAt; 
    }
    
    public LocalDateTime getActivatedAt() { return activatedAt; }
    public void setActivatedAt(LocalDateTime activatedAt) { 
        this.activatedAt = activatedAt; 
    }
    
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
}

/**
 * Infrastructure enum for JPA
 * Separate from domain enum to avoid coupling
 */
enum TicketTypeEntity {
    SINGLE, DAILY, WEEKLY, MONTHLY
}

enum TicketStatusEntity {
    PURCHASED, ACTIVE, USED, EXPIRED
}