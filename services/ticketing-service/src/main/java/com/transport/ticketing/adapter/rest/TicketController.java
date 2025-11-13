package com.transport.ticketing.adapter.rest;

import com.transport.ticketing.application.dto.*;
import com.transport.ticketing.application.usecase.*;
import com.transport.ticketing.domain.model.TicketStatus;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller for Ticket operations
 * 
 * Base URL: /api/tickets
 * 
 * Endpoints:
 * - POST   /purchase           - Purchase new tickets
 * - POST   /activate           - Activate a ticket
 * - POST   /validate           - Validate ticket on bus
 * - GET    /user/{userId}      - Get user's tickets
 * - GET    /{ticketId}         - Get specific ticket
 * - GET    /health             - Health check
 */
@RestController
@RequestMapping("/api/tickets")
@CrossOrigin(origins = "*") // Allow all origins for development
public class TicketController {
    
    private static final Logger logger = LoggerFactory.getLogger(TicketController.class);
    
    private final PurchaseTicketUseCase purchaseTicketUseCase;
    private final ActivateTicketUseCase activateTicketUseCase;
    private final ValidateTicketUseCase validateTicketUseCase;
    private final GetUserTicketsUseCase getUserTicketsUseCase;
    
    public TicketController(PurchaseTicketUseCase purchaseTicketUseCase,
                           ActivateTicketUseCase activateTicketUseCase,
                           ValidateTicketUseCase validateTicketUseCase,
                           GetUserTicketsUseCase getUserTicketsUseCase) {
        this.purchaseTicketUseCase = purchaseTicketUseCase;
        this.activateTicketUseCase = activateTicketUseCase;
        this.validateTicketUseCase = validateTicketUseCase;
        this.getUserTicketsUseCase = getUserTicketsUseCase;
    }
    
    /**
     * Purchase tickets
     * 
     * POST /api/tickets/purchase
     * 
     * Example request:
     * {
     *   "userId": "550e8400-e29b-41d4-a716-446655440000",
     *   "ticketType": "SINGLE",
     *   "quantity": 2
     * }
     */
    @PostMapping("/purchase")
    public ResponseEntity<PurchaseTicketResponse> purchaseTickets(
            @Valid @RequestBody PurchaseTicketRequest request) {
        
        logger.info("POST /api/tickets/purchase - User: {}, Type: {}, Qty: {}", 
                   request.getUserId(), request.getTicketType(), request.getQuantity());
        
        try {
            PurchaseTicketResponse response = purchaseTicketUseCase.execute(request);
            logger.info("Tickets purchased successfully. Order ID: {}", response.getOrderId());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            logger.error("Purchase failed: {}", e.getMessage());
            throw e; // GlobalExceptionHandler will catch this
        }
    }
    
    /**
     * Activate a ticket
     * 
     * POST /api/tickets/activate
     * 
     * Example request:
     * {
     *   "ticketId": "f47ac10b-58cc-4372-a567-0e02b2c3d479"
     * }
     */
    @PostMapping("/activate")
    public ResponseEntity<TicketResponse> activateTicket(
            @Valid @RequestBody ActivateTicketRequest request) {
        
        logger.info("POST /api/tickets/activate - Ticket ID: {}", request.getTicketId());
        
        try {
            TicketResponse response = activateTicketUseCase.execute(request);
            logger.info("Ticket activated successfully. Valid until: {}", 
                       response.getValidityEnd());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Activation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Activation failed - invalid state: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Validate a ticket (scan QR on bus)
     * 
     * POST /api/tickets/validate
     * 
     * Example request:
     * {
     *   "qrCode": "TICKET-f47ac10b-58cc-4372-a567-0e02b2c3d479",
     *   "busId": "a1b2c3d4-e5f6-4a5b-8c9d-0e1f2a3b4c5d",
     *   "lineNumber": "12"
     * }
     */
    @PostMapping("/validate")
    public ResponseEntity<TicketResponse> validateTicket(
            @Valid @RequestBody ValidateTicketRequest request) {
        
        logger.info("POST /api/tickets/validate - QR: {}, Bus: {}, Line: {}", 
                   request.getQrCode(), request.getBusId(), request.getLineNumber());
        
        try {
            TicketResponse response = validateTicketUseCase.execute(request);
            logger.info("Ticket validated successfully. Remaining: {}", 
                       response.getRemainingValidations());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed - ticket not found: {}", e.getMessage());
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Validation failed - invalid ticket state: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Get all tickets for a user
     * 
     * GET /api/tickets/user/{userId}
     * 
     * Optional query param: ?status=ACTIVE
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TicketResponse>> getUserTickets(
            @PathVariable UUID userId,
            @RequestParam(required = false) TicketStatus status) {
        
        logger.info("GET /api/tickets/user/{} - Status filter: {}", userId, status);
        
        List<TicketResponse> tickets;
        if (status != null) {
            tickets = getUserTicketsUseCase.executeByStatus(userId, status);
        } else {
            tickets = getUserTicketsUseCase.execute(userId);
        }
        
        logger.info("Found {} tickets for user {}", tickets.size(), userId);
        return ResponseEntity.ok(tickets);
    }
    
    /**
     * Get specific ticket by ID
     * 
     * GET /api/tickets/{ticketId}
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable UUID ticketId) {
        logger.info("GET /api/tickets/{}", ticketId);
        
        // This could be a separate use case, but keeping it simple
        // In production, create GetTicketByIdUseCase
        throw new UnsupportedOperationException("Not implemented yet - add GetTicketByIdUseCase");
    }
    
    /**
     * Health check endpoint
     * 
     * GET /api/tickets/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Ticketing Service is UP");
    }
}