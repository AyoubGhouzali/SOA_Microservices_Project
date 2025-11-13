package com.transport.ticketing.application.usecase;

import com.transport.ticketing.application.dto.TicketResponse;
import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.model.TicketStatus;
import com.transport.ticketing.domain.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Use Case: Get User's Tickets
 * 
 * Retrieves all tickets for a user, optionally filtered by status
 */
@Service
@Transactional(readOnly = true)
public class GetUserTicketsUseCase {
    
    private final TicketRepository ticketRepository;
    
    public GetUserTicketsUseCase(TicketRepository ticketRepository) {
        this.ticketRepository = ticketRepository;
    }
    
    public List<TicketResponse> execute(UUID userId) {
        List<Ticket> tickets = ticketRepository.findByUserId(userId);
        return tickets.stream()
                     .map(TicketResponse::fromDomain)
                     .toList();
    }
    
    public List<TicketResponse> executeByStatus(UUID userId, TicketStatus status) {
        List<Ticket> tickets = ticketRepository.findByUserIdAndStatus(userId, status);
        return tickets.stream()
                     .map(TicketResponse::fromDomain)
                     .toList();
    }
}