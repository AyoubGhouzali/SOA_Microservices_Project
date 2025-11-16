package com.transport.tracking.dto;

import com.transport.tracking.model.Bus;
import com.transport.tracking.model.BusStatus;
import com.transport.tracking.model.BusType;

import java.time.LocalDateTime;
import java.util.UUID;

public class BusResponse {

    private String id;
    private String busNumber;
    private String licensePlate;
    private BusType type;
    private BusStatus status;
    private UUID routeId;
    private String routeNumber;
    private Integer capacity;
    private Integer currentPassengers;
    private Double occupancyRate;
    private UUID driverId;
    private String driverName;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BusResponse fromDomain(Bus bus) {
        BusResponse response = new BusResponse();
        response.setId(bus.getId());
        response.setBusNumber(bus.getBusNumber());
        response.setLicensePlate(bus.getLicensePlate());
        response.setType(bus.getType());
        response.setStatus(bus.getStatus());
        response.setRouteId(bus.getRouteId());
        response.setRouteNumber(bus.getRouteNumber());
        response.setCapacity(bus.getCapacity());
        response.setCurrentPassengers(bus.getCurrentPassengers());
        response.setOccupancyRate(bus.getOccupancyRate());
        response.setDriverId(bus.getDriverId());
        response.setDriverName(bus.getDriverName());
        response.setLastMaintenanceDate(bus.getLastMaintenanceDate());
        response.setCreatedAt(bus.getCreatedAt());
        response.setUpdatedAt(bus.getUpdatedAt());
        return response;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public BusType getType() { return type; }
    public void setType(BusType type) { this.type = type; }

    public BusStatus getStatus() { return status; }
    public void setStatus(BusStatus status) { this.status = status; }

    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public String getRouteNumber() { return routeNumber; }
    public void setRouteNumber(String routeNumber) { this.routeNumber = routeNumber; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Integer getCurrentPassengers() { return currentPassengers; }
    public void setCurrentPassengers(Integer currentPassengers) {
        this.currentPassengers = currentPassengers;
    }

    public Double getOccupancyRate() { return occupancyRate; }
    public void setOccupancyRate(Double occupancyRate) { this.occupancyRate = occupancyRate; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public LocalDateTime getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDateTime lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}