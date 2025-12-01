package com.transport.ticketing.application.usecase;

import com.transport.ticketing.application.dto.PurchaseTicketRequest;
import com.transport.ticketing.application.dto.PurchaseTicketResponse;
import com.transport.ticketing.application.dto.TicketResponse;
import com.transport.ticketing.domain.event.TicketPurchasedEvent;
import com.transport.ticketing.domain.model.Ticket;
import com.transport.ticketing.domain.repository.TicketRepository;
import com.transport.ticketing.domain.service.PricingService;
import com.transport.ticketing.infrastructure.event.KafkaEventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Use Case: Purchase Tickets
 * 
 * Workflow:
 * 1. Calculate price
 * 2. Create ticket(s)
 * 3. Save to database
 * 4. Publish event to Kafka (for payment processing)
 * 5. Return response
 */
@Service
@Transactional
public class PurchaseTicketUseCase {
    
    private static final Logger logger = LoggerFactory.getLogger(PurchaseTicketUseCase.class);
    
    private final TicketRepository ticketRepository;
    private final PricingService pricingService;
    private final KafkaEventPublisher eventPublisher;
    
    public PurchaseTicketUseCase(TicketRepository ticketRepository,
                                  PricingService pricingService,
                                  KafkaEventPublisher eventPublisher) {
        this.ticketRepository = ticketRepository;
        this.pricingService = pricingService;
        this.eventPublisher = eventPublisher;
    }
    
    /**
     * Execute the purchase workflow
     */
    public PurchaseTicketResponse execute(PurchaseTicketRequest request) {
        logger.info("Processing ticket purchase for user: {}, type: {}, quantity: {}", 
                   request.getUserId(), request.getTicketType(), request.getQuantity());
        
        // 1. Calculate total price
        BigDecimal totalPrice = pricingService.calculatePrice(
            request.getTicketType(), 
            request.getQuantity()
        );
        
        logger.debug("Calculated total price: {} for {} tickets", 
                    totalPrice, request.getQuantity());
        
        // 2. Create order ID (groups all tickets in this purchase)
        UUID orderId = UUID.randomUUID();
        
        // 3. Create tickets
        List<Ticket> tickets = new ArrayList<>();
        BigDecimal pricePerTicket = totalPrice.divide(
            BigDecimal.valueOf(request.getQuantity()), 
            2, 
            BigDecimal.ROUND_HALF_UP
        );
        
        for (int i = 0; i < request.getQuantity(); i++) {
            Ticket ticket = createTicket(
                request.getUserId(), 
                orderId, 
                request.getTicketType(), 
                pricePerTicket
            );
            
            // Generate QR code
            ticket.setQrCode(generateQRCode(ticket.getId()));
            
            // Save ticket
            Ticket savedTicket = ticketRepository.save(ticket);
            tickets.add(savedTicket);
            
            logger.debug("Created ticket: {} with QR: {}", 
                        savedTicket.getId(), savedTicket.getQrCode());
        }
        
        // 4. Publish TicketPurchasedEvent to Kafka for payment processing
        publishTicketPurchasedEvent(tickets.get(0), orderId, totalPrice);
        
        // 5. Build response
        PurchaseTicketResponse response = new PurchaseTicketResponse();
        response.setOrderId(orderId);
        response.setUserId(request.getUserId());
        response.setTotalAmount(totalPrice);
        response.setCurrency("USD");
        response.setPaymentStatus("PENDING"); // Will be updated by Payment Service
        response.setTickets(tickets.stream()
                                  .map(TicketResponse::fromDomain)
                                  .toList());
        
        logger.info("Successfully created order: {} with {} tickets", 
                   orderId, tickets.size());
        
        return response;
    }
    
    /**
     * Create a single ticket
     */
    private Ticket createTicket(UUID userId, UUID orderId, 
                                com.transport.ticketing.domain.model.TicketType type, 
                                BigDecimal price) {
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setOrderId(orderId);
        ticket.setType(type);
        ticket.setPrice(price);
        return ticket;
    }
    
    /**
     * Generate QR code content
     * In production, this would use a real QR library
     */
    private String generateQRCode(UUID ticketId) {
        // Format: TICKET-{id}
        // In production, you'd use something like:
        // - ZXing library to generate actual QR image
        // - Store image in S3/Supabase Storage
        // - Return URL to image
        return "TICKET-" + ticketId.toString();
    }
    
    /**
     * Publish event for payment processing
     * Placeholder - we'll implement Kafka in next step
     */
    private void publishTicketPurchasedEvent(Ticket ticket, UUID orderId, 
                                        BigDecimal totalAmount) {
    // Create event
    TicketPurchasedEvent event = new TicketPurchasedEvent();
    event.setTicketId(ticket.getId());
    event.setUserId(ticket.getUserId());
    event.setOrderId(orderId);
    event.setTicketType(ticket.getType().name());
    event.setAmount(totalAmount);
    event.setCurrency(ticket.getCurrency());
    
    // Publish to Kafka
    eventPublisher.publishTicketPurchased(event);
    
    logger.info("Published TicketPurchasedEvent for order: {}", orderId);
}
}