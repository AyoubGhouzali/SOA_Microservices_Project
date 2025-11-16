package com.transport.scheduling.dto;

import com.transport.scheduling.model.ScheduleType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO pour créer un horaire
 */
public class CreateScheduleRequest {

    @NotNull(message = "Route ID is required")
    private UUID routeId;

    @NotNull(message = "Schedule type is required")
    private ScheduleType type;

    @NotNull(message = "Active days are required")
    private List<DayOfWeek> activeDays;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotNull(message = "Frequency is required")
    @Min(value = 1, message = "Frequency must be at least 1 minute")
    private Integer frequency;

    // Getters and Setters
    public UUID getRouteId() { return routeId; }
    public void setRouteId(UUID routeId) { this.routeId = routeId; }

    public ScheduleType getType() { return type; }
    public void setType(ScheduleType type) { this.type = type; }

    public List<DayOfWeek> getActiveDays() { return activeDays; }
    public void setActiveDays(List<DayOfWeek> activeDays) {
        this.activeDays = activeDays;
    }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public Integer getFrequency() { return frequency; }
    public void setFrequency(Integer frequency) { this.frequency = frequency; }
}