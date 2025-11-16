package com.transport.tracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;

import java.time.LocalDateTime;

/**
 * BusLocation Document - Position GPS d'un bus en temps réel
 * Stocké dans MongoDB avec TTL (Time To Live) pour archivage automatique
 */
@Document(collection = "bus_locations")
@CompoundIndexes({
        @CompoundIndex(name = "bus_timestamp_idx", def = "{'busId': 1, 'timestamp': -1}")
})
public class BusLocation {

    @Id
    private String id;

    @Indexed
    private String busId;

    private String busNumber;

    private String routeNumber;

    // Coordonnées GPS
    private Double latitude;

    private Double longitude;

    // Altitude (optionnel)
    private Double altitude;

    // Vitesse en km/h
    private Double speed;

    // Direction en degrés (0-360)
    private Double heading;

    // Précision GPS en mètres
    private Double accuracy;

    // Timestamp de la position
    @Indexed(expireAfterSeconds = 86400)  // TTL: 24 heures
    private LocalDateTime timestamp;

    // Informations contextuelles
    private Integer currentPassengers;

    private Double occupancyRate;

    private String nearestStop;

    private Double distanceToNextStop;  // en km

    private Integer estimatedArrivalMinutes;

    // Constructor
    public BusLocation() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getAltitude() { return altitude; }
    public void setAltitude(Double altitude) { this.altitude = altitude; }

    public Double getSpeed() { return speed; }
    public void setSpeed(Double speed) { this.speed = speed; }

    public Double getHeading() { return heading; }
    public void setHeading(Double heading) { this.heading = heading; }

    public Double getAccuracy() { return accuracy; }
    public void setAccuracy(Double accuracy) { this.accuracy = accuracy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Integer getCurrentPassengers() { return currentPassengers; }
    public void setCurrentPassengers(Integer currentPassengers) {
        this.currentPassengers = currentPassengers;
    }

    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public String getNearestStop() { return nearestStop; }
    public void setNearestStop(String nearestStop) { this.nearestStop = nearestStop; }

    public Double getDistanceToNextStop() { return distanceToNextStop; }
    public void setDistanceToNextStop(Double distanceToNextStop) {
        this.distanceToNextStop = distanceToNextStop;
    }

    public Integer getEstimatedArrivalMinutes() { return estimatedArrivalMinutes; }
    public void setEstimatedArrivalMinutes(Integer estimatedArrivalMinutes) {
        this.estimatedArrivalMinutes = estimatedArrivalMinutes;
    }
}