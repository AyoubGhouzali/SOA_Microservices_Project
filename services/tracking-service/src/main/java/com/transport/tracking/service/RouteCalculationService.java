package com.transport.tracking.service;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service de calcul d'itinéraires
 * Utilise Google Maps Directions API et OpenStreetMap
 */
@Service
public class RouteCalculationService {

    private static final Logger logger = LoggerFactory.getLogger(RouteCalculationService.class);

    private final GeoApiContext geoApiContext;
    private final WebClient openStreetMapClient;

    public RouteCalculationService(GeoApiContext geoApiContext, WebClient openStreetMapClient) {
        this.geoApiContext = geoApiContext;
        this.openStreetMapClient = openStreetMapClient;
    }

    /**
     * Calculer l'itinéraire avec Google Maps
     */
    public RouteInfo calculateRouteWithGoogleMaps(
            double originLat, double originLng,
            double destLat, double destLng) {
        try {
            logger.info("Calculating route with Google Maps: ({},{}) -> ({},{})",
                    originLat, originLng, destLat, destLng);

            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .mode(TravelMode.DRIVING)
                    .origin(new LatLng(originLat, originLng))
                    .destination(new LatLng(destLat, destLng))
                    .await();

            if (result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                DirectionsLeg leg = route.legs[0];

                long distanceMeters = leg.distance.inMeters;
                long durationSeconds = leg.duration.inSeconds;

                // Extraire les coordonnées du trajet
                List<Coordinate> path = extractPathFromRoute(route);

                RouteInfo routeInfo = new RouteInfo(
                        distanceMeters / 1000.0,  // km
                        durationSeconds / 60.0,    // minutes
                        path,
                        "GOOGLE_MAPS"
                );

                logger.info("Route calculated: {} km, {} minutes",
                        routeInfo.getDistanceKm(), routeInfo.getDurationMinutes());

                return routeInfo;
            }

            throw new IllegalArgumentException("Route not found");

        } catch (Exception e) {
            logger.error("Error calculating route with Google Maps: {}", e.getMessage());
            return calculateRouteWithOpenStreetMap(originLat, originLng, destLat, destLng);
        }
    }

    /**
     * Calculer l'itinéraire avec OpenStreetMap (OSRM)
     */
    public RouteInfo calculateRouteWithOpenStreetMap(
            double originLat, double originLng,
            double destLat, double destLng) {
        try {
            logger.info("Calculating route with OSRM: ({},{}) -> ({},{})",
                    originLat, originLng, destLat, destLng);

            // Utiliser OSRM (OpenStreetMap Routing Machine)
            String osrmUrl = "https://router.project-osrm.org";

            WebClient osrmClient = WebClient.builder().baseUrl(osrmUrl).build();

            Mono<Map> response = osrmClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/route/v1/driving/{coords}")
                            .queryParam("overview", "full")
                            .queryParam("geometries", "geojson")
                            .build(originLng + "," + originLat + ";" + destLng + "," + destLat))
                    .retrieve()
                    .bodyToMono(Map.class);

            Map result = response.block();

            if (result != null && result.containsKey("routes")) {
                List<Map> routes = (List<Map>) result.get("routes");
                if (!routes.isEmpty()) {
                    Map route = routes.get(0);

                    double distanceMeters = ((Number) route.get("distance")).doubleValue();
                    double durationSeconds = ((Number) route.get("duration")).doubleValue();

                    // Extraire le chemin
                    List<Coordinate> path = extractPathFromOSRM(route);

                    RouteInfo routeInfo = new RouteInfo(
                            distanceMeters / 1000.0,  // km
                            durationSeconds / 60.0,    // minutes
                            path,
                            "OPENSTREETMAP"
                    );

                    logger.info("Route calculated: {} km, {} minutes",
                            routeInfo.getDistanceKm(), routeInfo.getDurationMinutes());

                    return routeInfo;
                }
            }

            // Fallback: calcul simple en ligne droite
            return calculateStraightLineRoute(originLat, originLng, destLat, destLng);

        } catch (Exception e) {
            logger.error("Error calculating route with OSRM: {}", e.getMessage());
            return calculateStraightLineRoute(originLat, originLng, destLat, destLng);
        }
    }

    /**
     * Calcul de route simple en ligne droite (fallback)
     */
    private RouteInfo calculateStraightLineRoute(
            double originLat, double originLng,
            double destLat, double destLng) {

        double distance = calculateHaversineDistance(originLat, originLng, destLat, destLng);
        double duration = distance / 50.0 * 60; // Estimation: 50 km/h

        List<Coordinate> path = new ArrayList<>();
        path.add(new Coordinate(originLat, originLng));
        path.add(new Coordinate(destLat, destLng));

        return new RouteInfo(distance, duration, path, "STRAIGHT_LINE");
    }

    /**
     * Calculer la distance Haversine entre deux points
     */
    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Extraire le chemin depuis Google Maps route
     */
    private List<Coordinate> extractPathFromRoute(DirectionsRoute route) {
        List<Coordinate> path = new ArrayList<>();

        for (DirectionsLeg leg : route.legs) {
            for (DirectionsStep step : leg.steps) {
                LatLng start = step.startLocation;
                path.add(new Coordinate(start.lat, start.lng));
            }
        }

        return path;
    }

    /**
     * Extraire le chemin depuis OSRM
     */
    private List<Coordinate> extractPathFromOSRM(Map route) {
        List<Coordinate> path = new ArrayList<>();

        try {
            Map geometry = (Map) route.get("geometry");
            if (geometry != null && geometry.containsKey("coordinates")) {
                List<List<Number>> coordinates = (List<List<Number>>) geometry.get("coordinates");

                for (List<Number> coord : coordinates) {
                    double lng = coord.get(0).doubleValue();
                    double lat = coord.get(1).doubleValue();
                    path.add(new Coordinate(lat, lng));
                }
            }
        } catch (Exception e) {
            logger.error("Error extracting path from OSRM: {}", e.getMessage());
        }

        return path;
    }

    /**
     * Calculer le temps d'arrivée estimé (ETA)
     */
    public int calculateETA(double currentLat, double currentLng,
                            double destLat, double destLng,
                            double currentSpeedKmh) {

        RouteInfo route = calculateRouteWithOpenStreetMap(currentLat, currentLng, destLat, destLng);

        if (currentSpeedKmh > 0) {
            // Utiliser la vitesse actuelle
            double timeHours = route.getDistanceKm() / currentSpeedKmh;
            return (int) (timeHours * 60); // Convertir en minutes
        } else {
            // Utiliser le temps calculé par l'API
            return (int) route.getDurationMinutes();
        }
    }

    /**
     * Classe interne pour les informations de route
     */
    public static class RouteInfo {
        private double distanceKm;
        private double durationMinutes;
        private List<Coordinate> path;
        private String provider;

        public RouteInfo(double distanceKm, double durationMinutes,
                         List<Coordinate> path, String provider) {
            this.distanceKm = distanceKm;
            this.durationMinutes = durationMinutes;
            this.path = path;
            this.provider = provider;
        }

        // Getters
        public double getDistanceKm() { return distanceKm; }
        public double getDurationMinutes() { return durationMinutes; }
        public List<Coordinate> getPath() { return path; }
        public String getProvider() { return provider; }
    }

    /**
     * Classe interne pour les coordonnées
     */
    public static class Coordinate {
        private double latitude;
        private double longitude;

        public Coordinate(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
    }
}