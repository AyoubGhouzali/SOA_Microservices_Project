package com.transport.tracking.service;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Service de géocodage - Convertit adresses en coordonnées GPS
 * Supporte Google Maps API et OpenStreetMap (fallback gratuit)
 */
@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    private final GeoApiContext geoApiContext;
    private final WebClient openStreetMapClient;

    public GeocodingService(GeoApiContext geoApiContext, WebClient openStreetMapClient) {
        this.geoApiContext = geoApiContext;
        this.openStreetMapClient = openStreetMapClient;
    }

    /**
     * Géocoder une adresse avec Google Maps
     */
    public GeoLocation geocodeWithGoogleMaps(String address) {
        try {
            logger.info("Geocoding address with Google Maps: {}", address);

            GeocodingResult[] results = GeocodingApi.geocode(geoApiContext, address).await();

            if (results != null && results.length > 0) {
                LatLng location = results[0].geometry.location;
                String formattedAddress = results[0].formattedAddress;

                logger.info("Geocoded: {} -> ({}, {})", address, location.lat, location.lng);

                return new GeoLocation(
                        location.lat,
                        location.lng,
                        formattedAddress,
                        "GOOGLE_MAPS"
                );
            }

            throw new IllegalArgumentException("Address not found: " + address);

        } catch (Exception e) {
            logger.error("Error geocoding with Google Maps: {}", e.getMessage());
            // Fallback to OpenStreetMap
            return geocodeWithOpenStreetMap(address);
        }
    }

    /**
     * Géocoder une adresse avec OpenStreetMap (gratuit)
     */
    public GeoLocation geocodeWithOpenStreetMap(String address) {
        try {
            logger.info("Geocoding address with OpenStreetMap: {}", address);

            Mono<Map> response = openStreetMapClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search")
                            .queryParam("q", address)
                            .queryParam("format", "json")
                            .queryParam("limit", 1)
                            .build())
                    .retrieve()
                    .bodyToMono(Map[].class)
                    .map(results -> results.length > 0 ? results[0] : null);

            Map result = response.block();

            if (result != null) {
                double lat = Double.parseDouble(result.get("lat").toString());
                double lon = Double.parseDouble(result.get("lon").toString());
                String displayName = result.get("display_name").toString();

                logger.info("Geocoded: {} -> ({}, {})", address, lat, lon);

                return new GeoLocation(lat, lon, displayName, "OPENSTREETMAP");
            }

            throw new IllegalArgumentException("Address not found: " + address);

        } catch (Exception e) {
            logger.error("Error geocoding with OpenStreetMap: {}", e.getMessage());
            throw new RuntimeException("Failed to geocode address: " + address, e);
        }
    }

    /**
     * Géocodage inversé - Convertir coordonnées en adresse (Google Maps)
     */
    public String reverseGeocodeWithGoogleMaps(double latitude, double longitude) {
        try {
            logger.info("Reverse geocoding with Google Maps: ({}, {})", latitude, longitude);

            LatLng location = new LatLng(latitude, longitude);
            GeocodingResult[] results = GeocodingApi.reverseGeocode(geoApiContext, location).await();

            if (results != null && results.length > 0) {
                String address = results[0].formattedAddress;
                logger.info("Reverse geocoded: ({}, {}) -> {}", latitude, longitude, address);
                return address;
            }

            return "Unknown location";

        } catch (Exception e) {
            logger.error("Error reverse geocoding with Google Maps: {}", e.getMessage());
            return reverseGeocodeWithOpenStreetMap(latitude, longitude);
        }
    }

    /**
     * Géocodage inversé avec OpenStreetMap
     */
    public String reverseGeocodeWithOpenStreetMap(double latitude, double longitude) {
        try {
            logger.info("Reverse geocoding with OpenStreetMap: ({}, {})", latitude, longitude);

            Mono<Map> response = openStreetMapClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reverse")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("format", "json")
                            .build())
                    .retrieve()
                    .bodyToMono(Map.class);

            Map result = response.block();

            if (result != null && result.containsKey("display_name")) {
                String address = result.get("display_name").toString();
                logger.info("Reverse geocoded: ({}, {}) -> {}", latitude, longitude, address);
                return address;
            }

            return "Unknown location";

        } catch (Exception e) {
            logger.error("Error reverse geocoding with OpenStreetMap: {}", e.getMessage());
            return "Unknown location";
        }
    }

    /**
     * Classe interne pour les résultats de géocodage
     */
    public static class GeoLocation {
        private double latitude;
        private double longitude;
        private String formattedAddress;
        private String provider;

        public GeoLocation(double latitude, double longitude, String formattedAddress, String provider) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.formattedAddress = formattedAddress;
            this.provider = provider;
        }

        // Getters
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getFormattedAddress() { return formattedAddress; }
        public String getProvider() { return provider; }
    }
}