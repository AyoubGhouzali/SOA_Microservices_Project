package com.transport.scheduling.service;

import com.transport.scheduling.dto.AddStopRequest;
import com.transport.scheduling.dto.CreateRouteRequest;
import com.transport.scheduling.dto.RouteResponse;
import com.transport.scheduling.exception.ResourceAlreadyExistsException;
import com.transport.scheduling.exception.ResourceNotFoundException;
import com.transport.scheduling.model.Route;
import com.transport.scheduling.model.RouteStatus;
import com.transport.scheduling.model.Stop;
import com.transport.scheduling.repository.RouteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des lignes de bus
 */
@Service
@Transactional
public class RouteService {

    private static final Logger logger = LoggerFactory.getLogger(RouteService.class);

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    /**
     * Créer une nouvelle ligne
     */
    public RouteResponse createRoute(CreateRouteRequest request) {
        logger.info("Creating new route: {} - {}", request.getRouteNumber(), request.getName());

        // Vérifier si le numéro existe déjà
        if (routeRepository.existsByRouteNumber(request.getRouteNumber())) {
            throw new ResourceAlreadyExistsException(
                    "Route number already exists: " + request.getRouteNumber());
        }

        // Créer la ligne
        Route route = new Route();
        route.setRouteNumber(request.getRouteNumber());
        route.setName(request.getName());
        route.setDescription(request.getDescription());
        route.setType(request.getType());
        route.setColor(request.getColor() != null ? request.getColor() : "#000000");

        Route savedRoute = routeRepository.save(route);
        logger.info("Route created successfully: {}", savedRoute.getId());

        return RouteResponse.fromEntity(savedRoute);
    }

    /**
     * Récupérer toutes les lignes
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> getAllRoutes() {
        return routeRepository.findAll().stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer une ligne par ID
     */
    @Transactional(readOnly = true)
    public RouteResponse getRouteById(UUID id) {
        Route route = routeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + id));
        return RouteResponse.fromEntity(route);
    }

    /**
     * Récupérer une ligne par numéro
     */
    @Transactional(readOnly = true)
    public RouteResponse getRouteByNumber(String routeNumber) {
        Route route = routeRepository.findByRouteNumber(routeNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Route not found: " + routeNumber));
        return RouteResponse.fromEntity(route);
    }

    /**
     * Récupérer les lignes actives
     */
    @Transactional(readOnly = true)
    public List<RouteResponse> getActiveRoutes() {
        return routeRepository.findByStatus(RouteStatus.ACTIVE).stream()
                .map(RouteResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Ajouter un arrêt à une ligne
     */
    public RouteResponse addStopToRoute(AddStopRequest request) {
        logger.info("Adding stop to route {}: {}", request.getRouteId(), request.getName());

        Route route = routeRepository.findById(request.getRouteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Route not found: " + request.getRouteId()));

        Stop stop = new Stop(
                request.getName(),
                request.getLatitude(),
                request.getLongitude(),
                request.getSequenceOrder()
        );

        if (request.getDistanceToNext() != null) {
            stop.setDistanceToNext(request.getDistanceToNext());
        }

        if (request.getDurationToNext() != null) {
            stop.setDurationToNext(request.getDurationToNext());
        }

        route.addStop(stop);
        Route savedRoute = routeRepository.save(route);

        logger.info("Stop added successfully to route {}", savedRoute.getId());
        return RouteResponse.fromEntity(savedRoute);
    }

    /**
     * Activer une ligne
     */
    public RouteResponse activateRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + routeId));

        route.activate();
        Route savedRoute = routeRepository.save(route);

        logger.info("Route {} activated", routeId);
        return RouteResponse.fromEntity(savedRoute);
    }

    /**
     * Suspendre une ligne
     */
    public RouteResponse suspendRoute(UUID routeId) {
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new ResourceNotFoundException("Route not found: " + routeId));

        route.suspend();
        Route savedRoute = routeRepository.save(route);

        logger.info("Route {} suspended", routeId);
        return RouteResponse.fromEntity(savedRoute);
    }

    /**
     * Supprimer une ligne
     */
    public void deleteRoute(UUID routeId) {
        if (!routeRepository.existsById(routeId)) {
            throw new ResourceNotFoundException("Route not found: " + routeId);
        }

        routeRepository.deleteById(routeId);
        logger.info("Route {} deleted", routeId);
    }
}