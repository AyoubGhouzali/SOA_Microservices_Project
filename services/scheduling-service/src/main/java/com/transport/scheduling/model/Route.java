package com.transport.scheduling.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité Route - Représente une ligne de bus
 */
@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_route_number", columnList = "route_number", unique = true),
        @Index(name = "idx_route_status", columnList = "status")
})
public class Route {

    @Id
    @Column(name = "route_id")
    private UUID id;

    @Column(name = "route_number", nullable = false, unique = true, length = 10)
    private String routeNumber;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private RouteType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RouteStatus status;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    private List<Stop> stops = new ArrayList<>();

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "estimated_duration")
    private Integer estimatedDuration;

    @Column(name = "color", length = 7)
    private String color;

    public Route() {
        this.id = UUID.randomUUID();
        this.status = RouteStatus.ACTIVE;
    }

    // Méthodes métier

    public void addStop(Stop stop) {
        stops.add(stop);
        stop.setRoute(this);
        recalculateMetrics();
    }

    public void removeStop(Stop stop) {
        stops.remove(stop);
        stop.setRoute(null);
        recalculateMetrics();
    }

    public void activate() {
        if (stops.isEmpty()) {
            throw new IllegalStateException("Cannot activate route without stops");
        }
        this.status = RouteStatus.ACTIVE;
    }

    public void suspend() {
        this.status = RouteStatus.SUSPENDED;
    }

    public void setMaintenance() {
        this.status = RouteStatus.MAINTENANCE;
    }

    private void recalculateMetrics() {
        if (stops.isEmpty()) {
            this.totalDistance = 0.0;
            this.estimatedDuration = 0;
            return;
        }

        double distance = 0.0;
        for (int i = 0; i < stops.size() - 1; i++) {
            if (stops.get(i).getDistanceToNext() != null) {
                distance += stops.get(i).getDistanceToNext();
            }
        }
        this.totalDistance = distance;
        this.estimatedDuration = (int) (distance / 30.0 * 60); // 30 km/h moyenne
    }

    // Getters et Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RouteType getType() {
        return type;
    }

    public void setType(RouteType type) {
        this.type = type;
    }

    public RouteStatus getStatus() {
        return status;
    }

    public void setStatus(RouteStatus status) {
        this.status = status;
    }

    public List<Stop> getStops() {
        return stops;
    }

    public void setStops(List<Stop> stops) {
        this.stops = stops;
    }

    public Double getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}