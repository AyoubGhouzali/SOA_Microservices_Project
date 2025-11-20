package com.transport.tracking.controller;

import com.transport.tracking.service.RouteCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour le calcul d'itinéraires
 * Utilise Google Maps et OpenStreetMap
 */
@RestController
@RequestMapping("/api/routesBus")
@CrossOrigin(origins = "*")
public class RouteController {

    private static final Logger logger = LoggerFactory.getLogger(RouteController.class);

    private final RouteCalculationService routeCalculationService;

    public RouteController(RouteCalculationService routeCalculationService) {
        this.routeCalculationService = routeCalculationService;
    }

    /**
     * Calculer un itinéraire entre deux points
     * GET /api/routes/calculate?originLat=33.5731&originLng=-7.5898&destLat=33.5937&destLng=-7.6187&provider=osm
     */
    @GetMapping("/calculate")
    public ResponseEntity<RouteCalculationService.RouteInfo> calculateRoute(
            @RequestParam double originLat,
            @RequestParam double originLng,
            @RequestParam double destLat,
            @RequestParam double destLng,
            @RequestParam(defaultValue = "osm") String provider) {

        logger.info("GET /api/routes/calculate - ({},{}) -> ({},{}) provider={}",
                originLat, originLng, destLat, destLng, provider);

        RouteCalculationService.RouteInfo routeInfo;

        if ("google".equalsIgnoreCase(provider)) {
            routeInfo = routeCalculationService.calculateRouteWithGoogleMaps(
                    originLat, originLng, destLat, destLng);
        } else {
            routeInfo = routeCalculationService.calculateRouteWithOpenStreetMap(
                    originLat, originLng, destLat, destLng);
        }

        return ResponseEntity.ok(routeInfo);
    }

    /**
     * Calculer le temps d'arrivée estimé (ETA)
     * GET /api/routes/eta?currentLat=33.5731&currentLng=-7.5898&destLat=33.5937&destLng=-7.6187&speedKmh=45
     */
    @GetMapping("/eta")
    public ResponseEntity<ETAResponse> calculateETA(
            @RequestParam double currentLat,
            @RequestParam double currentLng,
            @RequestParam double destLat,
            @RequestParam double destLng,
            @RequestParam(defaultValue = "0") double speedKmh) {

        logger.info("GET /api/routes/eta - ({},{}) -> ({},{}) speed={}km/h",
                currentLat, currentLng, destLat, destLng, speedKmh);

        int etaMinutes = routeCalculationService.calculateETA(
                currentLat, currentLng, destLat, destLng, speedKmh);

        return ResponseEntity.ok(new ETAResponse(etaMinutes));
    }

    // Inner class pour la réponse ETA
    public static class ETAResponse {
        private int estimatedMinutes;
        private String message;

        public ETAResponse(int estimatedMinutes) {
            this.estimatedMinutes = estimatedMinutes;
            this.message = String.format("Arrival in approximately %d minutes", estimatedMinutes);
        }

        public int getEstimatedMinutes() { return estimatedMinutes; }
        public String getMessage() { return message; }
    }
}