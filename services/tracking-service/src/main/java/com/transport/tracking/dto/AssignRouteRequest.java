package com.transport.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AssignRouteRequest {

    @NotBlank(message = "Bus ID is required")
    private String busId;

    @NotNull(message = "Route ID is required")
    private UUID routeId;

    @NotBlank(message = "Route number is required")
    private String routeNumber;

    // Getters and Setters
    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }
}