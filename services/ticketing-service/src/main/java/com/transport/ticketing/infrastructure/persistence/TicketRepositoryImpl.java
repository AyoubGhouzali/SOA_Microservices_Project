package com.transport.ticketing.infrastructure.persistence;

import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.model.TicketStatus;
import com.transport.ticketing.domain.model.TicketType;
import com.transport.ticketing.domain.repository.TicketRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementing the domain port
 * 
 * Responsibilities:
 * 1. Convert Domain objects → JPA entities (toEntity)
 * 2. Convert JPA entities → Domain objects (toDomain)
 * 3. Delegate persistence to Spring Data JPA
 */
@Repository
public class TicketRepositoryImpl implements TicketRepository {
    
    private final JpaTicketRepository jpaRepository;
    
    public TicketRepositoryImpl(JpaTicketRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }
    
    @Override
    public Ticket save(Ticket ticket) {
        TicketEntity entity = toEntity(ticket);
        TicketEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }
    
    @Override
    public Optional<Ticket> findById(UUID id) {
        return jpaRepository.findById(id)
                           .map(this::toDomain);
    }
    
    @Override
    public List<Ticket> findByUserId(UUID userId) {
        return jpaRepository.findByUserId(userId)
                           .stream()
                           .map(this::toDomain)
                           .collect(Collectors.toList());
    }
    
    @Override
    public List<Ticket> findByUserIdAndStatus(UUID userId, TicketStatus status) {
        TicketStatusEntity entityStatus = TicketStatusEntity.valueOf(status.name());
        return jpaRepository.findByUserIdAndStatus(userId, entityStatus)
                           .stream()
                           .map(this::toDomain)
                           .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Ticket> findByQrCode(String qrCode) {
        return jpaRepository.findByQrCode(qrCode)
                           .map(this::toDomain);
    }
    
    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }
    
    // ============================================
    // MAPPING METHODS
    // ============================================
    
    /**
     * Convert domain object to JPA entity
     */
    private TicketEntity toEntity(Ticket ticket) {
        TicketEntity entity = new TicketEntity();
        entity.setId(ticket.getId());
        entity.setUserId(ticket.getUserId());
        entity.setOrderId(ticket.getOrderId());
        entity.setType(TicketTypeEntity.valueOf(ticket.getType().name()));
        entity.setStatus(TicketStatusEntity.valueOf(ticket.getStatus().name()));
        entity.setPrice(ticket.getPrice());
        entity.setCurrency(ticket.getCurrency());
        entity.setValidityStart(ticket.getValidityStart());
        entity.setValidityEnd(ticket.getValidityEnd());
        entity.setRemainingValidations(ticket.getRemainingValidations());
        entity.setPurchasedAt(ticket.getPurchasedAt());
        entity.setActivatedAt(ticket.getActivatedAt());
        entity.setQrCode(ticket.getQrCode());
        return entity;
    }
    
    /**
     * Convert JPA entity to domain object
     */
    private Ticket toDomain(TicketEntity entity) {
        Ticket ticket = new Ticket();
        ticket.setId(entity.getId());
        ticket.setUserId(entity.getUserId());
        ticket.setOrderId(entity.getOrderId());
        ticket.setType(TicketType.valueOf(entity.getType().name()));
        ticket.setStatus(TicketStatus.valueOf(entity.getStatus().name()));
        ticket.setPrice(entity.getPrice());
        ticket.setCurrency(entity.getCurrency());
        ticket.setValidityStart(entity.getValidityStart());
        ticket.setValidityEnd(entity.getValidityEnd());
        ticket.setRemainingValidations(entity.getRemainingValidations());
        // Note: purchasedAt is set in Ticket constructor, but we override it
        ticket.setPurchasedAt(entity.getPurchasedAt());
        ticket.setActivatedAt(entity.getActivatedAt());
        ticket.setQrCode(entity.getQrCode());
        return ticket;
    }
}