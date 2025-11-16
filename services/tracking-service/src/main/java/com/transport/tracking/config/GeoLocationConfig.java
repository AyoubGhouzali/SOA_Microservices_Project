package com.transport.tracking.config;

import com.google.maps.GeoApiContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GeoLocationConfig {

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Value("${openstreetmap.api.url:https://nominatim.openstreetmap.org}")
    private String openStreetMapApiUrl;

    /**
     * Google Maps API Context
     * Utilisé pour les services de géocodage, directions, etc.
     */
    @Bean
    public GeoApiContext geoApiContext() {
        GeoApiContext.Builder builder = new GeoApiContext.Builder();

        if (googleMapsApiKey != null && !googleMapsApiKey.isEmpty() && !googleMapsApiKey.equals("YOUR_API_KEY")) {
            builder.apiKey(googleMapsApiKey);
        }

        return builder.build();
    }

    /**
     * WebClient pour OpenStreetMap
     * Alternative gratuite à Google Maps
     */
    @Bean
    public WebClient openStreetMapClient() {
        return WebClient.builder()
                .baseUrl(openStreetMapApiUrl)
                .defaultHeader("User-Agent", "UrbanTransportSystem/1.0")
                .build();
    }
}