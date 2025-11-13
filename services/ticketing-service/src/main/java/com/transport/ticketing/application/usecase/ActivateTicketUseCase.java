package com.transport.ticketing.application.usecase;

import com.transport.ticketing.application.dto.ActivateTicketRequest;
import com.transport.ticketing.application.dto.TicketResponse;
import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use Case: Activate Ticket
 * 
 * Activates a purchased ticket, starting its validity period
 */
@Service
@Transactional
public class ActivateTicketUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(ActivateTicketUseCase.class);
    
    private final TicketRepository ticketRepository;
    
    public ActivateTicketUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    public TicketResponse execute(ActivateTicketRequest request) {
        logger.info("Activating ticket: {}", request.getTicketId());
        
        // 1. Find ticket
        Ticket ticket = ticketRepository.findById(request.getTicketId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Ticket not found: " + request.getTicketId()
            ));
        
        // 2. Activate (domain method handles business rules)
        ticket.activate();
        
        // 3. Save
        Ticket savedTicket = ticketRepository.save(ticket);
        
        logger.info("Ticket {} activated. Valid until: {}", 
                   savedTicket.getId(), savedTicket.getValidityEnd());
        
        // 4. Return response
        return TicketResponse.fromDomain(savedTicket);
    }
}
