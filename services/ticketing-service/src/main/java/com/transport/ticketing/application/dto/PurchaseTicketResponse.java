package com.transport.ticketing.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Response for ticket purchase containing order details
 */
public class PurchaseTicketResponse {
    private UUID orderId;
    private UUID userId;
    private List<TicketResponse> tickets;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentStatus;  // PENDING, COMPLETED, FAILED
    
    // Getters and Setters
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public List<TicketResponse> getTickets() { return tickets; }
    public void setTickets(List<TicketResponse> tickets) { this.tickets = tickets; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { 
        this.paymentStatus = paymentStatus; 
    }
}