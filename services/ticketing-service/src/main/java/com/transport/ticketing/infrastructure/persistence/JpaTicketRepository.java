package com.transport.ticketing.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository
 * Spring auto-generates implementation at runtime
 */
@Repository
public interface JpaTicketRepository extends JpaRepository<TicketEntity, UUID> {
    
    // Spring auto-implements these based on method names!
    List<TicketEntity> findByUserId(UUID userId);
    
    List<TicketEntity> findByUserIdAndStatus(UUID userId, TicketStatusEntity status);
    
    Optional<TicketEntity> findByQrCode(String qrCode);
    
    List<TicketEntity> findByOrderId(UUID orderId);
}