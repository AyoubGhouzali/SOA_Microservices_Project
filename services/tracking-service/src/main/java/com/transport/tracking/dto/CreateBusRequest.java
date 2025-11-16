package com.transport.tracking.dto;

import com.transport.tracking.model.BusType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class CreateBusRequest {

    @NotBlank(message = "Bus number is required")
    private String busNumber;

    @NotBlank(message = "License plate is required")
    private String licensePlate;

    @NotNull(message = "Bus type is required")
    private BusType type;

    @NotNull(message = "Capacity is required")
    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    // Getters and Setters
    public String getBusNumber() { return busNumber; }
    public void setBusNumber(String busNumber) { this.busNumber = busNumber; }

    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }

    public BusType getType() { return type; }
    public void setType(BusType type) { this.type = type; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
}