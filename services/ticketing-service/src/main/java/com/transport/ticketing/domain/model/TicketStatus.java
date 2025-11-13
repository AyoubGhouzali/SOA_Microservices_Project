package com.transport.ticketing.domain.model;

public enum TicketStatus {
    PURCHASED,  // Just bought, not yet activated
    ACTIVE,     // Activated and valid
    USED,       // Already validated (for SINGLE tickets)
    EXPIRED     // Validity period ended
}