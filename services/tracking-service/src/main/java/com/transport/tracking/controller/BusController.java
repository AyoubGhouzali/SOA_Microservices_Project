package com.transport.tracking.controller;

import com.transport.tracking.dto.*;
import com.transport.tracking.model.BusStatus;
import com.transport.tracking.service.BusService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des bus
 *
 * Base URL: /api/buses
 */
@RestController
@RequestMapping("/api/buses")
@CrossOrigin(origins = "*")
public class BusController {

    private static final Logger logger = LoggerFactory.getLogger(BusController.class);

    private final BusService busService;

    public BusController(BusService busService) {
        this.busService = busService;
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tracking Service - Buses API is UP");
    }

    /**
     * Créer un nouveau bus
     * POST /api/buses
     */
    @PostMapping
    public ResponseEntity<BusResponse> createBus(@Valid @RequestBody CreateBusRequest request) {
        logger.info("POST /api/buses - Creating bus: {}", request.getBusNumber());
        BusResponse response = busService.createBus(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer tous les bus
     * GET /api/buses
     */
    @GetMapping
    public ResponseEntity<List<BusResponse>> getAllBuses() {
        logger.info("GET /api/buses - Fetching all buses");
        List<BusResponse> buses = busService.getAllBuses();
        return ResponseEntity.ok(buses);
    }

    /**
     * Récupérer un bus par ID
     * GET /api/buses/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BusResponse> getBusById(@PathVariable String id) {
        logger.info("GET /api/buses/{} - Fetching bus", id);
        BusResponse bus = busService.getBusById(id);
        return ResponseEntity.ok(bus);
    }

    /**
     * Récupérer un bus par numéro
     * GET /api/buses/number/{busNumber}
     */
    @GetMapping("/number/{busNumber}")
    public ResponseEntity<BusResponse> getBusByNumber(@PathVariable String busNumber) {
        logger.info("GET /api/buses/number/{} - Fetching bus", busNumber);
        BusResponse bus = busService.getBusByNumber(busNumber);
        return ResponseEntity.ok(bus);
    }

    /**
     * Récupérer les bus par statut
     * GET /api/buses/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BusResponse>> getBusesByStatus(@PathVariable BusStatus status) {
        logger.info("GET /api/buses/status/{} - Fetching buses", status);
        List<BusResponse> buses = busService.getBusesByStatus(status);
        return ResponseEntity.ok(buses);
    }

    /**
     * Récupérer les bus d'une ligne
     * GET /api/buses/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<BusResponse>> getBusesByRoute(@PathVariable UUID routeId) {
        logger.info("GET /api/buses/route/{} - Fetching buses", routeId);
        List<BusResponse> buses = busService.getBusesByRoute(routeId);
        return ResponseEntity.ok(buses);
    }

    /**
     * Assigner une ligne à un bus
     * POST /api/buses/assign-route
     */
    @PostMapping("/assign-route")
    public ResponseEntity<BusResponse> assignRoute(@Valid @RequestBody AssignRouteRequest request) {
        logger.info("POST /api/buses/assign-route - Assigning route to bus");
        BusResponse response = busService.assignRoute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Assigner un conducteur à un bus
     * POST /api/buses/assign-driver
     */
    @PostMapping("/assign-driver")
    public ResponseEntity<BusResponse> assignDriver(@Valid @RequestBody AssignDriverRequest request) {
        logger.info("POST /api/buses/assign-driver - Assigning driver to bus");
        BusResponse response = busService.assignDriver(
                request.getBusId(),
                request.getDriverId(),
                request.getDriverName()
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Démarrer le service d'un bus
     * PUT /api/buses/{id}/start-service
     */
    @PutMapping("/{id}/start-service")
    public ResponseEntity<BusResponse> startService(@PathVariable String id) {
        logger.info("PUT /api/buses/{}/start-service", id);
        BusResponse response = busService.startService(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Terminer le service d'un bus
     * PUT /api/buses/{id}/end-service
     */
    @PutMapping("/{id}/end-service")
    public ResponseEntity<BusResponse> endService(@PathVariable String id) {
        logger.info("PUT /api/buses/{}/end-service", id);
        BusResponse response = busService.endService(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre un bus en maintenance
     * PUT /api/buses/{id}/maintenance
     */
    @PutMapping("/{id}/maintenance")
    public ResponseEntity<BusResponse> setMaintenance(@PathVariable String id) {
        logger.info("PUT /api/buses/{}/maintenance", id);
        BusResponse response = busService.setMaintenance(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour le nombre de passagers
     * PUT /api/buses/{id}/passengers/{count}
     */
    @PutMapping("/{id}/passengers/{count}")
    public ResponseEntity<BusResponse> updatePassengerCount(
            @PathVariable String id,
            @PathVariable int count) {
        logger.info("PUT /api/buses/{}/passengers/{}", id, count);
        BusResponse response = busService.updatePassengerCount(id, count);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un bus
     * DELETE /api/buses/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBus(@PathVariable String id) {
        logger.info("DELETE /api/buses/{}", id);
        busService.deleteBus(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir des statistiques
     * GET /api/buses/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<BusService.BusStatistics> getStatistics() {
        logger.info("GET /api/buses/statistics");
        BusService.BusStatistics stats = busService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}