package com.transport.scheduling.dto;

import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO de réponse pour le prochain départ
 */
public class NextDepartureResponse {
    private UUID routeId;
    private String routeNumber;
    private String routeName;
    private LocalTime nextDeparture;
    private Integer minutesUntilDeparture;

    // Getters and Setters
    public UUID getRouteId() {
        return routeId;
    }

    public void setRouteId(UUID routeId) {
        this.routeId = routeId;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    public void setRouteNumber(String routeNumber) {
        this.routeNumber = routeNumber;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public LocalTime getNextDeparture() {
        return nextDeparture;
    }

    public void setNextDeparture(LocalTime nextDeparture) {
        this.nextDeparture = nextDeparture;
    }

    public Integer getMinutesUntilDeparture() {
        return minutesUntilDeparture;
    }

    public void setMinutesUntilDeparture(Integer minutesUntilDeparture) {
        this.minutesUntilDeparture = minutesUntilDeparture;
    }
}