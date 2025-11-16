package com.transport.scheduling.service;

import com.transport.scheduling.dto.CreateScheduleRequest;
import com.transport.scheduling.dto.NextDepartureResponse;
import com.transport.scheduling.dto.ScheduleResponse;
import com.transport.scheduling.exception.ResourceNotFoundException;
import com.transport.scheduling.model.Route;
import com.transport.scheduling.model.Schedule;
import com.transport.scheduling.repository.RouteRepository;
import com.transport.scheduling.repository.ScheduleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des horaires
 */
@Service
@Transactional
public class ScheduleService {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleService.class);

    private final ScheduleRepository scheduleRepository;
    private final RouteRepository routeRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
                           RouteRepository routeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.routeRepository = routeRepository;
    }

    /**
     * Créer un horaire
     */
    public ScheduleResponse createSchedule(CreateScheduleRequest request) {
        logger.info("Creating schedule for route: {}", request.getRouteId());

        // Vérifier que la ligne existe
        routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Route not found: " + request.getRouteId()));

        // Valider les heures
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        // Créer l'horaire
        Schedule schedule = new Schedule();
        schedule.setRouteId(request.getRouteId());
        schedule.setType(request.getType());
        schedule.setActiveDays(request.getActiveDays());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setFrequency(request.getFrequency());

        // Générer les horaires de départ
        schedule.generateDepartureTimes();

        Schedule savedSchedule = scheduleRepository.save(schedule);

        logger.info("Schedule created successfully: {} with {} departures",
                savedSchedule.getId(),
                savedSchedule.getDepartureTimes().size());

        return ScheduleResponse.fromEntity(savedSchedule);
    }

    /**
     * Récupérer tous les horaires d'une ligne
     */
    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedulesByRoute(UUID routeId) {
        return scheduleRepository.findByRouteId(routeId).stream()
                .map(ScheduleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un horaire par ID
     */
    @Transactional(readOnly = true)
    public ScheduleResponse getScheduleById(UUID id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found: " + id));
        return ScheduleResponse.fromEntity(schedule);
    }

    /**
     * Obtenir le prochain départ pour une ligne
     */
    @Transactional(readOnly = true)
    public NextDepartureResponse getNextDeparture(UUID routeId) {
        // Récupérer la ligne
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + routeId));

        // Récupérer le jour actuel
        DayOfWeek today = DayOfWeek.from(java.time.LocalDate.now());
        LocalTime now = LocalTime.now();

        // Trouver les horaires actifs pour aujourd'hui
        List<Schedule> schedules = scheduleRepository
                .findActiveSchedulesByRouteIdAndDay(routeId, today);

        if (schedules.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No active schedule found for route " + route.getRouteNumber() + " today");
        }

        // Chercher le prochain départ
        LocalTime nextDeparture = null;
        for (Schedule schedule : schedules) {
            LocalTime departure = schedule.getNextDeparture(now);
            if (departure != null && (nextDeparture == null || departure.isBefore(nextDeparture))) {
                nextDeparture = departure;
            }
        }

        if (nextDeparture == null) {
            throw new ResourceNotFoundException(
                    "No more departures today for route " + route.getRouteNumber());
        }

        // Calculer les minutes restantes
        int minutesUntil = (int) now.until(nextDeparture, ChronoUnit.MINUTES);

        // Créer la réponse
        NextDepartureResponse response = new NextDepartureResponse();
        response.setRouteId(route.getId());
        response.setRouteNumber(route.getRouteNumber());
        response.setRouteName(route.getName());
        response.setNextDeparture(nextDeparture);
        response.setMinutesUntilDeparture(minutesUntil);

        return response;
    }

    /**
     * Activer un horaire
     */
    public ScheduleResponse activateSchedule(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found: " + scheduleId));

        schedule.activate();
        Schedule savedSchedule = scheduleRepository.save(schedule);

        logger.info("Schedule {} activated", scheduleId);
        return ScheduleResponse.fromEntity(savedSchedule);
    }

    /**
     * Désactiver un horaire
     */
    public ScheduleResponse deactivateSchedule(UUID scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found: " + scheduleId));

        schedule.deactivate();
        Schedule savedSchedule = scheduleRepository.save(schedule);

        logger.info("Schedule {} deactivated", scheduleId);
        return ScheduleResponse.fromEntity(savedSchedule);
    }

    /**
     * Supprimer un horaire
     */
    public void deleteSchedule(UUID scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            throw new ResourceNotFoundException("Schedule not found: " + scheduleId);
        }

        scheduleRepository.deleteById(scheduleId);
        logger.info("Schedule {} deleted", scheduleId);
    }
}