package com.transport.tracking.controller;

import com.transport.tracking.dto.LocationResponse;
import com.transport.tracking.dto.UpdateLocationRequest;
import com.transport.tracking.service.TrackingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller pour le tracking GPS des bus
 *
 * Base URL: /api/tracking
 */
@RestController
@RequestMapping("/api/tracking")
@CrossOrigin(origins = "*")
public class TrackingController {

    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);

    private final TrackingService trackingService;

    public TrackingController(TrackingService trackingService) {
        this.trackingService = trackingService;
    }

    /**
     * Health Check
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tracking Service - Tracking API is UP");
    }

    /**
     * Mettre à jour la position d'un bus
     * POST /api/tracking/location
     *
     * Body:
     * {
     *   "busId": "bus-id",
     *   "latitude": 33.5731,
     *   "longitude": -7.5898,
     *   "speed": 45.5,
     *   "heading": 180.0,
     *   "currentPassengers": 25
     * }
     */
    @PostMapping("/location")
    public ResponseEntity<LocationResponse> updateLocation(
            @Valid @RequestBody UpdateLocationRequest request) {
        logger.info("POST /api/tracking/location - Updating location for bus: {}", request.getBusId());
        LocationResponse response = trackingService.updateLocation(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir la dernière position d'un bus
     * GET /api/tracking/location/{busId}
     */
    @GetMapping("/location/{busId}")
    public ResponseEntity<LocationResponse> getLatestLocation(@PathVariable String busId) {
        logger.info("GET /api/tracking/location/{} - Fetching latest location", busId);
        LocationResponse response = trackingService.getLatestLocation(busId);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtenir l'historique des positions d'un bus
     * GET /api/tracking/history/{busId}?start=2024-01-15T10:00:00&end=2024-01-15T18:00:00
     */
    @GetMapping("/history/{busId}")
    public ResponseEntity<List<LocationResponse>> getLocationHistory(
            @PathVariable String busId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        logger.info("GET /api/tracking/history/{} - From {} to {}", busId, start, end);
        List<LocationResponse> history = trackingService.getLocationHistory(busId, start, end);
        return ResponseEntity.ok(history);
    }

    /**
     * Obtenir les positions de tous les bus d'une ligne
     * GET /api/tracking/route/{routeNumber}
     */
    @GetMapping("/route/{routeNumber}")
    public ResponseEntity<List<LocationResponse>> getLocationsByRoute(@PathVariable String routeNumber) {
        logger.info("GET /api/tracking/route/{} - Fetching bus locations", routeNumber);
        List<LocationResponse> locations = trackingService.getLocationsByRoute(routeNumber);
        return ResponseEntity.ok(locations);
    }

    /**
     * Obtenir toutes les positions récentes
     * GET /api/tracking/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<LocationResponse>> getRecentLocations() {
        logger.info("GET /api/tracking/recent - Fetching all recent locations");
        List<LocationResponse> locations = trackingService.getRecentLocations();
        return ResponseEntity.ok(locations);
    }

    /**
     * Trouver les bus à proximité d'un point
     * GET /api/tracking/nearby?latitude=33.5731&longitude=-7.5898&radius=5
     */
    @GetMapping("/nearby")
    public ResponseEntity<List<LocationResponse>> findNearbyBuses(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radius) {
        logger.info("GET /api/tracking/nearby - lat={}, lon={}, radius={}km",
                latitude, longitude, radius);
        List<LocationResponse> buses = trackingService.findNearbyBuses(latitude, longitude, radius);
        return ResponseEntity.ok(buses);
    }

    /**
     * Calculer la distance entre deux points
     * GET /api/tracking/distance?lat1=33.5731&lon1=-7.5898&lat2=33.5937&lon2=-7.6187
     */
    @GetMapping("/distance")
    public ResponseEntity<DistanceResponse> calculateDistance(
            @RequestParam double lat1,
            @RequestParam double lon1,
            @RequestParam double lat2,
            @RequestParam double lon2) {
        logger.info("GET /api/tracking/distance - Calculating distance");
        double distance = trackingService.calculateDistance(lat1, lon1, lat2, lon2);
        return ResponseEntity.ok(new DistanceResponse(distance));
    }

    // Inner class pour la réponse de distance
    public static class DistanceResponse {
        private double distanceKm;

        public DistanceResponse(double distanceKm) {
            this.distanceKm = distanceKm;
        }

        public double getDistanceKm() { return distanceKm; }
        public void setDistanceKm(double distanceKm) { this.distanceKm = distanceKm; }
    }
}