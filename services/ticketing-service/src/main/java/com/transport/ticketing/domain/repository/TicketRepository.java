package com.transport.ticketing.domain.repository;

import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.model.TicketStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository port - domain defines what it needs
 * Infrastructure will implement this
 */
public interface TicketRepository {
    Ticket save(Ticket ticket);
    Optional<Ticket> findById(UUID id);
    List<Ticket> findByUserId(UUID userId);
    List<Ticket> findByUserIdAndStatus(UUID userId, TicketStatus status);
    Optional<Ticket> findByQrCode(String qrCode);
    void delete(UUID id);
}