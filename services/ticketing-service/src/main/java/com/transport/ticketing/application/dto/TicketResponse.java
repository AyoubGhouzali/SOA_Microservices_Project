package com.transport.ticketing.application.dto;

import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.model.TicketStatus;
import com.transport.ticketing.domain.model.TicketType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ticket information
 * This is what the REST API returns
 */
public class TicketResponse {
    private UUID id;
    private UUID userId;
    private UUID orderId;
    private TicketType type;
    private TicketStatus status;
    private BigDecimal price;
    private String currency;
    private LocalDateTime validityStart;
    private LocalDateTime validityEnd;
    private int remainingValidations;
    private LocalDateTime purchasedAt;
    private LocalDateTime activatedAt;
    private String qrCode;
    
    /**
     * Factory method - converts domain object to DTO
     * This is the ONLY way to create a TicketResponse from domain
     */
    public static TicketResponse fromDomain(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setUserId(ticket.getUserId());
        response.setOrderId(ticket.getOrderId());
        response.setType(ticket.getType());
        response.setStatus(ticket.getStatus());
        response.setPrice(ticket.getPrice());
        response.setCurrency(ticket.getCurrency());
        response.setValidityStart(ticket.getValidityStart());
        response.setValidityEnd(ticket.getValidityEnd());
        response.setRemainingValidations(ticket.getRemainingValidations());
        response.setPurchasedAt(ticket.getPurchasedAt());
        response.setActivatedAt(ticket.getActivatedAt());
        response.setQrCode(ticket.getQrCode());
        return response;
    }
    
    // Getters and Setters
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