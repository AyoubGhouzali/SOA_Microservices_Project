package com.transport.tracking.kafka;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

/**
 * DTO pour les événements de bus envoyés via Kafka
 * Ce format sera utilisé par le service de géolocalisation (Producer)
 * et reçu par le service de notifications (Consumer)
 */
public class BusEventNotification {

    // Identifiant unique de l'événement
    private String eventId;

    // Type d'événement (DELAY, CANCELLATION, etc.)
    private BusEventType eventType;

    // Informations du bus
    private String busId;
    private String busNumber;
    private String routeNumber;

    // Détails de l'événement
    private String title;
    private String message;

    // Pour les retards : durée en minutes
    private Integer delayMinutes;

    // Localisation
    private Double latitude;
    private Double longitude;
    private String location;

    // Timestamp de l'événement
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Priorité (LOW, MEDIUM, HIGH, URGENT)
    private String priority;

    // Liste des utilisateurs à notifier
    private String affectedUsers;

    // Métadonnées additionnelles
    private String metadata;

    // Constructeurs
    public BusEventNotification() {
        this.timestamp = LocalDateTime.now();
    }

    // Builder pattern
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final BusEventNotification event;

        public Builder() {
            this.event = new BusEventNotification();
        }

        public Builder eventId(String eventId) {
            event.eventId = eventId;
            return this;
        }

        public Builder eventType(BusEventType eventType) {
            event.eventType = eventType;
            return this;
        }

        public Builder busId(String busId) {
            event.busId = busId;
            return this;
        }

        public Builder busNumber(String busNumber) {
            event.busNumber = busNumber;
            return this;
        }

        public Builder routeNumber(String routeNumber) {
            event.routeNumber = routeNumber;
            return this;
        }

        public Builder title(String title) {
            event.title = title;
            return this;
        }

        public Builder message(String message) {
            event.message = message;
            return this;
        }

        public Builder delayMinutes(Integer delayMinutes) {
            event.delayMinutes = delayMinutes;
            return this;
        }

        public Builder latitude(Double latitude) {
            event.latitude = latitude;
            return this;
        }

        public Builder longitude(Double longitude) {
            event.longitude = longitude;
            return this;
        }

        public Builder location(String location) {
            event.location = location;
            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {
            event.timestamp = timestamp;
            return this;
        }

        public Builder priority(String priority) {
            event.priority = priority;
            return this;
        }

        public Builder affectedUsers(String affectedUsers) {
            event.affectedUsers = affectedUsers;
            return this;
        }

        public Builder metadata(String metadata) {
            event.metadata = metadata;
            return this;
        }

        public BusEventNotification build() {
            return event;
        }
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public BusEventType getEventType() { return eventType; }
    public void setEventType(BusEventType eventType) { this.eventType = eventType; }

    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Integer getDelayMinutes() { return delayMinutes; }
    public void setDelayMinutes(Integer delayMinutes) { this.delayMinutes = delayMinutes; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getAffectedUsers() { return affectedUsers; }
    public void setAffectedUsers(String affectedUsers) { this.affectedUsers = affectedUsers; }

    public String getMetadata() { return metadata; }
    public void setMetadata(String metadata) { this.metadata = metadata; }
}
