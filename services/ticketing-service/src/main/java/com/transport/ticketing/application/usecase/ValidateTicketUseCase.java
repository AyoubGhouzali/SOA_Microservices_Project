package com.transport.ticketing.application.usecase;

import com.transport.ticketing.application.dto.ValidateTicketRequest;
import com.transport.ticketing.application.dto.TicketResponse;
import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Validate Ticket
 * 
 * Used when driver scans passenger's QR code
 * Validates ticket is active and has remaining uses
 */
@Service
@Transactional
public class ValidateTicketUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidateTicketUseCase.class);
    
    private final TicketRepository ticketRepository;
    
    public ValidateTicketUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    public TicketResponse execute(ValidateTicketRequest request) {
        logger.info("Validating ticket with QR: {} on bus: {}, line: {}", 
                   request.getQrCode(), request.getBusId(), request.getLineNumber());
        
        // 1. Find ticket by QR code
        Ticket ticket = ticketRepository.findByQrCode(request.getQrCode())
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid QR code: " + request.getQrCode()
            ));
        
        // 2. Validate ticket (domain method handles all business rules)
        try {
            ticket.validate(request.getBusId(), request.getLineNumber());
            logger.info("Ticket {} validated successfully. Remaining: {}", 
                       ticket.getId(), ticket.getRemainingValidations());
        } catch (IllegalStateException e) {
            logger.warn("Ticket validation failed: {}", e.getMessage());
            throw e; // Re-throw to send error to driver app
        }
        
        // 3. Save updated ticket
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // 4. Return response
        return TicketResponse.fromDomain(savedTicket);
    }
}