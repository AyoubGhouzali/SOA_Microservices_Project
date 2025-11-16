package com.transport.scheduling.controller;

import com.transport.scheduling.dto.AddStopRequest;
import com.transport.scheduling.dto.CreateRouteRequest;
import com.transport.scheduling.dto.RouteResponse;
import com.transport.scheduling.service.RouteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des lignes de bus
 *
 * Base URL: /api/routes
 */
@RestController
@RequestMapping("/api/routes")
@CrossOrigin(origins = "*")
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    /**
     * Créer une nouvelle ligne
     *
     * POST /api/routes
     *
     * Body:
     * {
     *   "routeNumber": "12",
     *   "name": "Centre-ville - Aéroport",
     *   "description": "Ligne express vers l'aéroport",
     *   "type": "EXPRESS",
     *   "color": "#FF5733"
     * }
     */
    @PostMapping
    public ResponseEntity<RouteResponse> createRoute(
            @Valid @RequestBody CreateRouteRequest request) {

        logger.info("POST /api/routes - Creating route: {}", request.getRouteNumber());

        RouteResponse response = routeService.createRoute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer toutes les lignes
     *
     * GET /api/routes
     */
    @GetMapping
    public ResponseEntity<List<RouteResponse>> getAllRoutes() {
        logger.info("GET /api/routes - Fetching all routes");

        List<RouteResponse> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(routes);
    }

    /**
     * Récupérer les lignes actives
     *
     * GET /api/routes/active
     */
    @GetMapping("/active")
    public ResponseEntity<List<RouteResponse>> getActiveRoutes() {
        logger.info("GET /api/routes/active - Fetching active routes");

        List<RouteResponse> routes = routeService.getActiveRoutes();
        return ResponseEntity.ok(routes);
    }

    /**
     * Récupérer une ligne par ID
     *
     * GET /api/routes/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RouteResponse> getRouteById(@PathVariable UUID id) {
        logger.info("GET /api/routes/{} - Fetching route", id);

        RouteResponse route = routeService.getRouteById(id);
        return ResponseEntity.ok(route);
    }

    /**
     * Récupérer une ligne par numéro
     *
     * GET /api/routes/number/{routeNumber}
     */
    @GetMapping("/number/{routeNumber}")
    public ResponseEntity<RouteResponse> getRouteByNumber(@PathVariable String routeNumber) {
        logger.info("GET /api/routes/number/{} - Fetching route", routeNumber);

        RouteResponse route = routeService.getRouteByNumber(routeNumber);
        return ResponseEntity.ok(route);
    }

    /**
     * Ajouter un arrêt à une ligne
     *
     * POST /api/routes/stops
     *
     * Body:
     * {
     *   "routeId": "uuid",
     *   "name": "Gare Centrale",
     *   "latitude": 33.5731,
     *   "longitude": -7.5898,
     *   "sequenceOrder": 1,
     *   "distanceToNext": 2.5,
     *   "durationToNext": 5
     * }
     */
    @PostMapping("/stops")
    public ResponseEntity<RouteResponse> addStop(
            @Valid @RequestBody AddStopRequest request) {

        logger.info("POST /api/routes/stops - Adding stop {} to route {}",
                request.getName(), request.getRouteId());

        RouteResponse response = routeService.addStopToRoute(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Activer une ligne
     *
     * PUT /api/routes/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<RouteResponse> activateRoute(@PathVariable UUID id) {
        logger.info("PUT /api/routes/{}/activate - Activating route", id);

        RouteResponse response = routeService.activateRoute(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspendre une ligne
     *
     * PUT /api/routes/{id}/suspend
     */
    @PutMapping("/{id}/suspend")
    public ResponseEntity<RouteResponse> suspendRoute(@PathVariable UUID id) {
        logger.info("PUT /api/routes/{}/suspend - Suspending route", id);

        RouteResponse response = routeService.suspendRoute(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer une ligne
     *
     * DELETE /api/routes/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoute(@PathVariable UUID id) {
        logger.info("DELETE /api/routes/{} - Deleting route", id);

        routeService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     *
     * GET /api/routes/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Scheduling Service - Routes API is UP");
    }
}