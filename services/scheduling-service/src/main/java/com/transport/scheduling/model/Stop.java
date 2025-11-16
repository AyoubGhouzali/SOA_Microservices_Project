package com.transport.scheduling.model;

import jakarta.persistence.*;

import java.util.UUID;

/**
 * Entité Stop - Représente un arrêt de bus sur une ligne
 */
@Entity
@Table(name = "stops", indexes = {
        @Index(name = "idx_stop_route_id", columnList = "route_id"),
        @Index(name = "idx_stop_sequence", columnList = "route_id, sequence_order")
})
public class Stop {

    @Id
    @Column(name = "stop_id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "sequence_order", nullable = false)
    private Integer sequenceOrder;

    @Column(name = "distance_to_next")
    private Double distanceToNext;

    @Column(name = "duration_to_next")
    private Integer durationToNext;

    public Stop() {
        this.id = UUID.randomUUID();
    }

    public Stop(String name, Double latitude, Double longitude, Integer sequenceOrder) {
        this();
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.sequenceOrder = sequenceOrder;
    }

    // Méthode métier : Calculer la distance vers un autre arrêt (formule de Haversine)
    public double calculateDistanceTo(Stop other) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(other.latitude - this.latitude);
        double lonDistance = Math.toRadians(other.longitude - this.longitude);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude))
                * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    // Getters et Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Route getRoute() {
        return route;
    }

    public void setRoute(Route route) {
        this.route = route;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getSequenceOrder() {
        return sequenceOrder;
    }

    public void setSequenceOrder(Integer sequenceOrder) {
        this.sequenceOrder = sequenceOrder;
    }

    public Double getDistanceToNext() {
        return distanceToNext;
    }

    public void setDistanceToNext(Double distanceToNext) {
        this.distanceToNext = distanceToNext;
    }

    public Integer getDurationToNext() {
        return durationToNext;
    }

    public void setDurationToNext(Integer durationToNext) {
        this.durationToNext = durationToNext;
    }
}