package com.transport.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class AssignDriverRequest {

    @NotBlank(message = "Bus ID is required")
    private String busId;

    @NotNull(message = "Driver ID is required")
    private UUID driverId;

    @NotBlank(message = "Driver name is required")
    private String driverName;

    // Getters and Setters
    public String getBusId() { return busId; }
    public void setBusId(String busId) { this.busId = busId; }

    public UUID getDriverId() { return driverId; }
    public void setDriverId(UUID driverId) { this.driverId = driverId; }

    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
}