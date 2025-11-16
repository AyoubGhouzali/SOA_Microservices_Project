package com.transport.scheduling.controller;

import com.transport.scheduling.dto.CreateScheduleRequest;
import com.transport.scheduling.dto.NextDepartureResponse;
import com.transport.scheduling.dto.ScheduleResponse;
import com.transport.scheduling.service.ScheduleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des horaires
 *
 * Base URL: /api/schedules
 */
@RestController
@RequestMapping("/api/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleController.class);

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    /**
     * Créer un nouvel horaire
     *
     * POST /api/schedules
     *
     * Body:
     * {
     *   "routeId": "uuid",
     *   "type": "WEEKDAY",
     *   "activeDays": ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
     *   "startTime": "06:00",
     *   "endTime": "22:00",
     *   "frequency": 15
     * }
     */
    @PostMapping
    public ResponseEntity<ScheduleResponse> createSchedule(
            @Valid @RequestBody CreateScheduleRequest request) {

        logger.info("POST /api/schedules - Creating schedule for route: {}",
                request.getRouteId());

        ScheduleResponse response = scheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Récupérer un horaire par ID
     *
     * GET /api/schedules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ScheduleResponse> getScheduleById(@PathVariable UUID id) {
        logger.info("GET /api/schedules/{} - Fetching schedule", id);

        ScheduleResponse schedule = scheduleService.getScheduleById(id);
        return ResponseEntity.ok(schedule);
    }

    /**
     * Récupérer tous les horaires d'une ligne
     *
     * GET /api/schedules/route/{routeId}
     */
    @GetMapping("/route/{routeId}")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByRoute(
            @PathVariable UUID routeId) {

        logger.info("GET /api/schedules/route/{} - Fetching schedules", routeId);

        List<ScheduleResponse> schedules = scheduleService.getSchedulesByRoute(routeId);
        return ResponseEntity.ok(schedules);
    }

    /**
     * Obtenir le prochain départ pour une ligne
     *
     * GET /api/schedules/route/{routeId}/next-departure
     *
     * Retourne:
     * {
     *   "routeId": "uuid",
     *   "routeNumber": "12",
     *   "routeName": "Centre-ville - Aéroport",
     *   "nextDeparture": "14:30",
     *   "minutesUntilDeparture": 15
     * }
     */
    @GetMapping("/route/{routeId}/next-departure")
    public ResponseEntity<NextDepartureResponse> getNextDeparture(
            @PathVariable UUID routeId) {

        logger.info("GET /api/schedules/route/{}/next-departure - Getting next departure",
                routeId);

        NextDepartureResponse response = scheduleService.getNextDeparture(routeId);
        return ResponseEntity.ok(response);
    }

    /**
     * Activer un horaire
     *
     * PUT /api/schedules/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<ScheduleResponse> activateSchedule(@PathVariable UUID id) {
        logger.info("PUT /api/schedules/{}/activate - Activating schedule", id);

        ScheduleResponse response = scheduleService.activateSchedule(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Désactiver un horaire
     *
     * PUT /api/schedules/{id}/deactivate
     */
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ScheduleResponse> deactivateSchedule(@PathVariable UUID id) {
        logger.info("PUT /api/schedules/{}/deactivate - Deactivating schedule", id);

        ScheduleResponse response = scheduleService.deactivateSchedule(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Supprimer un horaire
     *
     * DELETE /api/schedules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable UUID id) {
        logger.info("DELETE /api/schedules/{} - Deleting schedule", id);

        scheduleService.deleteSchedule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check
     *
     * GET /api/schedules/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Scheduling Service - Schedules API is UP");
    }
}