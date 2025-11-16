package com.transport.scheduling.dto;

import com.transport.scheduling.model.Schedule;
import com.transport.scheduling.model.ScheduleType;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse pour Schedule
 */
public class ScheduleResponse {
    private UUID id;
    private UUID routeId;
    private ScheduleType type;
    private List<DayOfWeek> activeDays;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer frequency;
    private List<LocalTime> departureTimes;
    private Boolean isActive;

    public static ScheduleResponse fromEntity(Schedule schedule) {
        ScheduleResponse response = new ScheduleResponse();
        response.setId(schedule.getId());
        response.setRouteId(schedule.getRouteId());
        response.setType(schedule.getType());
        response.setActiveDays(schedule.getActiveDays());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());
        response.setFrequency(schedule.getFrequency());
        response.setDepartureTimes(schedule.getDepartureTimes());
        response.setIsActive(schedule.getIsActive());
        return response;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getRouteId() {
        return routeId;
    }

    public void setRouteId(UUID routeId) {
        this.routeId = routeId;
    }

    public ScheduleType getType() {
        return type;
    }

    public void setType(ScheduleType type) {
        this.type = type;
    }

    public List<DayOfWeek> getActiveDays() {
        return activeDays;
    }

    public void setActiveDays(List<DayOfWeek> activeDays) {
        this.activeDays = activeDays;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public List<LocalTime> getDepartureTimes() {
        return departureTimes;
    }

    public void setDepartureTimes(List<LocalTime> departureTimes) {
        this.departureTimes = departureTimes;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}