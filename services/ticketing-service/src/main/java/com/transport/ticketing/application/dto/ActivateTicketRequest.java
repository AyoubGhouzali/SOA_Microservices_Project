package com.transport.ticketing.application.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to activate a ticket
 */
public class ActivateTicketRequest {
    
    @NotNull(message = "Ticket ID is required")
    private UUID ticketId;
    
    // Getters and Setters
    public UUID getTicketId() { return ticketId; }
    public void setTicketId(UUID ticketId) { this.ticketId = ticketId; }
}