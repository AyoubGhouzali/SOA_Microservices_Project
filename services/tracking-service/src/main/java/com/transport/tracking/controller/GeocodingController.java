package com.transport.tracking.controller;

import com.transport.tracking.service.GeocodingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour les services de géocodage
 * Convertit adresses en coordonnées GPS et vice-versa
 */
@RestController
@RequestMapping("/api/geocoding")
@CrossOrigin(origins = "*")
public class GeocodingController {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingController.class);

    private final GeocodingService geocodingService;

    public GeocodingController(GeocodingService geocodingService) {
        this.geocodingService = geocodingService;
    }

    /**
     * Géocoder une adresse -> coordonnées GPS
     * GET /api/geocoding/address?q=Place Mohammed V, Casablanca&provider=google
     */
    @GetMapping("/address")
    public ResponseEntity<GeocodingService.GeoLocation> geocodeAddress(
            @RequestParam String q,
            @RequestParam(defaultValue = "osm") String provider) {
        logger.info("GET /api/geocoding/address - q={}, provider={}", q, provider);

        GeocodingService.GeoLocation location;

        if ("google".equalsIgnoreCase(provider)) {
            location = geocodingService.geocodeWithGoogleMaps(q);
        } else {
            location = geocodingService.geocodeWithOpenStreetMap(q);
        }

        return ResponseEntity.ok(location);
    }

    /**
     * Géocodage inversé: coordonnées GPS -> adresse
     * GET /api/geocoding/reverse?lat=33.5731&lng=-7.5898&provider=osm
     */
    @GetMapping("/reverse")
    public ResponseEntity<AddressResponse> reverseGeocode(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "osm") String provider) {
        logger.info("GET /api/geocoding/reverse - lat={}, lng={}, provider={}", lat, lng, provider);

        String address;

        if ("google".equalsIgnoreCase(provider)) {
            address = geocodingService.reverseGeocodeWithGoogleMaps(lat, lng);
        } else {
            address = geocodingService.reverseGeocodeWithOpenStreetMap(lat, lng);
        }

        return ResponseEntity.ok(new AddressResponse(lat, lng, address, provider));
    }

    // Inner class pour la réponse
    public static class AddressResponse {
        private double latitude;
        private double longitude;
        private String address;
        private String provider;

        public AddressResponse(double latitude, double longitude, String address, String provider) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.provider = provider;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getAddress() { return address; }
        public String getProvider() { return provider; }
    }
}