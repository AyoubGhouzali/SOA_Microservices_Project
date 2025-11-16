package com.transport.scheduling.model;

import jakarta.persistence.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entité Schedule - Représente les horaires de passage pour une ligne
 */
@Entity
@Table(name = "schedules", indexes = {
        @Index(name = "idx_schedule_route_id", columnList = "route_id"),
        @Index(name = "idx_schedule_type", columnList = "type"),
        @Index(name = "idx_schedule_active", columnList = "is_active")
})
public class Schedule {

    @Id
    @Column(name = "schedule_id")
    private UUID id;

    @Column(name = "route_id", nullable = false)
    private UUID routeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ScheduleType type;

    @ElementCollection
    @CollectionTable(name = "schedule_active_days", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "day_of_week")
    @Enumerated(EnumType.STRING)
    private List<DayOfWeek> activeDays = new ArrayList<>();

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "frequency", nullable = false)
    private Integer frequency; // En minutes

    @ElementCollection
    @CollectionTable(name = "schedule_departure_times", joinColumns = @JoinColumn(name = "schedule_id"))
    @Column(name = "departure_time")
    @OrderBy
    private List<LocalTime> departureTimes = new ArrayList<>();

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    public Schedule() {
        this.id = UUID.randomUUID();
        this.isActive = true;
    }

    // Méthodes métier

    /**
     * Générer les horaires de départ basés sur la fréquence
     */
    public void generateDepartureTimes() {
        if (startTime == null || endTime == null || frequency == null) {
            throw new IllegalStateException(
                    "Start time, end time, and frequency must be set");
        }

        departureTimes.clear();
        LocalTime current = startTime;

        while (current.isBefore(endTime) || current.equals(endTime)) {
            departureTimes.add(current);
            current = current.plusMinutes(frequency);
        }
    }

    /**
     * Ajouter un horaire de départ spécifique
     */
    public void addDepartureTime(LocalTime time) {
        if (!departureTimes.contains(time)) {
            departureTimes.add(time);
            departureTimes.sort(LocalTime::compareTo);
        }
    }

    /**
     * Obtenir le prochain départ après une heure donnée
     */
    public LocalTime getNextDeparture(LocalTime currentTime) {
        for (LocalTime departure : departureTimes) {
            if (departure.isAfter(currentTime)) {
                return departure;
            }
        }
        return null;
    }

    /**
     * Vérifier si l'horaire est actif pour un jour donné
     */
    public boolean isActiveOnDay(DayOfWeek day) {
        return isActive && activeDays.contains(day);
    }

    public void activate() {
        this.isActive = true;
    }

    public void deactivate() {
        this.isActive = false;
    }

    // Getters et Setters

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