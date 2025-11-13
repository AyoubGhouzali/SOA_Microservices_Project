package com.transport.ticketing.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Request to validate a ticket on a bus
 * Used by driver app when passenger scans QR code
 */
public class ValidateTicketRequest {
    
    @NotBlank(message = "QR code is required")
    private String qrCode;
    
    @NotNull(message = "Bus ID is required")
    private UUID busId;
    
    @NotBlank(message = "Line number is required")
    private String lineNumber;
    
    // Getters and Setters
    public String getQrCode() { return qrCode; }
    public void setQrCode(String qrCode) { this.qrCode = qrCode; }
    
    public UUID getBusId() { return busId; }
    public void setBusId(UUID busId) { this.busId = busId; }
    
    public String getLineNumber() { return lineNumber; }
    public void setLineNumber(String lineNumber) { this.lineNumber = lineNumber; }
}
