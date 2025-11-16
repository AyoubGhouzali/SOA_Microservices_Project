package com.transport.scheduling.dto;

import com.transport.scheduling.model.Route;
import com.transport.scheduling.model.RouteStatus;
import com.transport.scheduling.model.RouteType;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * DTO de réponse pour Route
 */
public class RouteResponse {
    private UUID id;
    private String routeNumber;
    private String name;
    private String description;
    private RouteType type;
    private RouteStatus status;
    private List<StopResponse> stops;
    private Double totalDistance;
    private Integer estimatedDuration;
    private String color;

    // Factory method
    public static RouteResponse fromEntity(Route route) {
        RouteResponse response = new RouteResponse();
        response.setId(route.getId());
        response.setRouteNumber(route.getRouteNumber());
        response.setName(route.getName());
        response.setDescription(route.getDescription());
        response.setType(route.getType());
        response.setStatus(route.getStatus());
        response.setTotalDistance(route.getTotalDistance());
        response.setEstimatedDuration(route.getEstimatedDuration());
        response.setColor(route.getColor());

        if (route.getStops() != null) {
            response.setStops(route.getStops().stream()
                    .map(StopResponse::fromEntity)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public RouteType getType() { return type; }
    public void setType(RouteType type) { this.type = type; }

    public RouteStatus getStatus() { return status; }
    public void setStatus(RouteStatus status) { this.status = status; }

    public List<StopResponse> getStops() { return stops; }
    public void setStops(List<StopResponse> stops) { this.stops = stops; }

    public Double getTotalDistance() { return totalDistance; }
    public void setTotalDistance(Double totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getEstimatedDuration() { return estimatedDuration; }
    public void setEstimatedDuration(Integer estimatedDuration) {
        this.estimatedDuration = estimatedDuration;
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}