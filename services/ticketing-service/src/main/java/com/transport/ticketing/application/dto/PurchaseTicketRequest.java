package com.transport.ticketing.application.dto;

import com.transport.ticketing.domain.model.TicketType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request DTO for purchasing tickets
 * This is what the REST API receives
 */
public class PurchaseTicketRequest {
    
    @NotNull(message = "User ID is required")
    private UUID userId;
    
    @NotNull(message = "Ticket type is required")
    private TicketType ticketType;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity = 1;
    
    // Optional: payment method info (for future integration)
    private String paymentMethodId;
    
    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public TicketType getTicketType() { return ticketType; }
    public void setTicketType(TicketType ticketType) { this.ticketType = ticketType; }
    
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { 
        this.paymentMethodId = paymentMethodId; 
    }
}