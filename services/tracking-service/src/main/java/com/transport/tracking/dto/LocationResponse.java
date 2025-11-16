package com.transport.tracking.dto;

import com.transport.tracking.model.BusLocation;

import java.time.LocalDateTime;

public class LocationResponse {

    private String id;
    private String busId;
    private String busNumber;
    private String routeNumber;
    private Double latitude;
    private Double longitude;
    private Double altitude;
    private Double speed;
    private Double heading;
    private Double accuracy;
    private LocalDateTime timestamp;
    private Integer currentPassengers;
    private Double occupancyRate;
    private String nearestStop;
    private Double distanceToNextStop;
    private Integer estimatedArrivalMinutes;

    public static LocationResponse fromDomain(BusLocation location) {
        LocationResponse response = new LocationResponse();
        response.setId(location.getId());
        response.setBusId(location.getBusId());
        response.setBusNumber(location.getBusNumber());
        response.setRouteNumber(location.getRouteNumber());
        response.setLatitude(location.getLatitude());
        response.setLongitude(location.getLongitude());
        response.setAltitude(location.getAltitude());
        response.setSpeed(location.getSpeed());
        response.setHeading(location.getHeading());
        response.setAccuracy(location.getAccuracy());
        response.setTimestamp(location.getTimestamp());
        response.setCurrentPassengers(location.getCurrentPassengers());
        response.setOccupancyRate(location.getOccupancyRate());
        response.setNearestStop(location.getNearestStop());
        response.setDistanceToNextStop(location.getDistanceToNextStop());
        response.setEstimatedArrivalMinutes(location.getEstimatedArrivalMinutes());
        return response;
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
        this.estimatedArrivalMinutes = estimatedArrivalMinutes; }
}