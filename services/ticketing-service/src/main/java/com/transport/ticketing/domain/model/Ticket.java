package com.transport.ticketing.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Ticket Aggregate Root
 * Contains all business logic for ticket lifecycle
 * NO framework dependencies (pure Java)
 */
public class Ticket {
    private UUID id;
    private UUID userId;
    private UUID orderId;
    private TicketType type;
    private TicketStatus status;
    private BigDecimal price;
    private String currency;
    private LocalDateTime validityStart;
    private LocalDateTime validityEnd;
    private int remainingValidations;  // For multi-use tickets
    private LocalDateTime purchasedAt;
    private LocalDateTime activatedAt;
    private String qrCode;
    
    // Constructor
    public Ticket() {
        this.id = UUID.randomUUID();
        this.purchasedAt = LocalDateTime.now();
        this.status = TicketStatus.PURCHASED;
        this.currency = "USD";
    }
    
    // ============================================
    // BUSINESS METHODS (Domain Logic)
    // ============================================
    
    /**
     * Activate the ticket - starts validity period
     */
    public void activate() {
        if (this.status != TicketStatus.PURCHASED) {
            throw new IllegalStateException("Only purchased tickets can be activated");
        }
        
        this.status = TicketStatus.ACTIVE;
        this.activatedAt = LocalDateTime.now();
        this.validityStart = LocalDateTime.now();
        this.validityEnd = calculateValidityEnd();
        
        // Set remaining validations based on ticket type
        this.remainingValidations = switch (this.type) {
            case SINGLE -> 1;
            case DAILY, WEEKLY, MONTHLY -> Integer.MAX_VALUE; // Unlimited during period
        };
    }
    
    /**
     * Validate ticket on bus - consumes one use
     * @param busId The bus where ticket is being validated
     * @param lineNumber The bus line number
     */
    public void validate(UUID busId, String lineNumber) {
        // Check if ticket is active
        if (this.status != TicketStatus.ACTIVE) {
            throw new IllegalStateException("Ticket is not active. Status: " + this.status);
        }
        
        // Check if expired
        if (LocalDateTime.now().isAfter(this.validityEnd)) {
            this.status = TicketStatus.EXPIRED;
            throw new IllegalStateException("Ticket has expired");
        }
        
        // Check remaining validations
        if (this.remainingValidations <= 0) {
            this.status = TicketStatus.USED;
            throw new IllegalStateException("No validations remaining");
        }
        
        // Consume one validation
        this.remainingValidations--;
        
        // Mark as USED if no validations left (for SINGLE tickets)
        if (this.remainingValidations == 0 && this.type == TicketType.SINGLE) {
            this.status = TicketStatus.USED;
        }
        
        // In a real system, you'd emit a TicketValidatedEvent here
        // containing busId, lineNumber, timestamp for analytics
    }
    
    /**
     * Check if ticket is currently valid
     */
    public boolean isValid() {
        return this.status == TicketStatus.ACTIVE 
            && LocalDateTime.now().isBefore(this.validityEnd)
            && this.remainingValidations > 0;
    }
    
    /**
     * Calculate when ticket expires based on type
     */
    private LocalDateTime calculateValidityEnd() {
        return switch (this.type) {
            case SINGLE -> validityStart.plusHours(2);
            case DAILY -> validityStart.plusDays(1);
            case WEEKLY -> validityStart.plusDays(7);
            case MONTHLY -> validityStart.plusMonths(1);
        };
    }
    
    // ============================================
    // GETTERS AND SETTERS
    // ============================================
    
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public TicketType getType() { return type; }
    public void setType(TicketType type) { this.type = type; }
    
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    
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