package com.transport.tracking.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bus Document - Représente un bus dans le système
 * Stocké dans MongoDB
 */
@Document(collection = "buses")
public class Bus {

    @Id
    private String id;

    @Indexed(unique = true)
    private String busNumber;

    private String licensePlate;

    private BusType type;

    private BusStatus status;

    private UUID routeId;  // Référence vers Scheduling Service

    private String routeNumber;

    private Integer capacity;

    private Integer currentPassengers;

    private UUID driverId;  // Référence vers User Service

    private String driverName;

    private LocalDateTime lastMaintenanceDate;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Constructor
    public Bus() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = BusStatus.INACTIVE;
        this.currentPassengers = 0;
    }

    // Business methods
    public void assignRoute(UUID routeId, String routeNumber) {
        this.routeId = routeId;
        this.routeNumber = routeNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void assignDriver(UUID driverId, String driverName) {
        this.driverId = driverId;
        this.driverName = driverName;
        this.updatedAt = LocalDateTime.now();
    }

    public void startService() {
        if (this.status == BusStatus.IN_SERVICE) {
            throw new IllegalStateException("Bus is already in service");
        }
        if (this.driverId == null) {
            throw new IllegalStateException("Cannot start service without a driver");
        }
        if (this.routeId == null) {
            throw new IllegalStateException("Cannot start service without a route");
        }
        this.status = BusStatus.IN_SERVICE;
        this.updatedAt = LocalDateTime.now();
    }

    public void endService() {
        this.status = BusStatus.INACTIVE;
        this.currentPassengers = 0;
        this.updatedAt = LocalDateTime.now();
    }

    public void setMaintenance() {
        this.status = BusStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassengerCount(int count) {
        if (count < 0 || count > this.capacity) {
            throw new IllegalArgumentException("Invalid passenger count");
        }
        this.currentPassengers = count;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isAvailable() {
        return this.status == BusStatus.INACTIVE;
    }

    public boolean isInService() {
        return this.status == BusStatus.IN_SERVICE;
    }

    public double getOccupancyRate() {
        if (this.capacity == null || this.capacity == 0) {
            return 0.0;
        }
        return (double) this.currentPassengers / this.capacity * 100;
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