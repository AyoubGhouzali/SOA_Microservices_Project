package com.transport.tracking.service;

import com.transport.tracking.dto.*;
import com.transport.tracking.model.Bus;
import com.transport.tracking.model.BusStatus;
import com.transport.tracking.repository.BusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BusService {

    private static final Logger logger = LoggerFactory.getLogger(BusService.class);

    private final BusRepository busRepository;

    public BusService(BusRepository busRepository) {
        this.busRepository = busRepository;
    }

    /**
     * Créer un nouveau bus
     */
    public BusResponse createBus(CreateBusRequest request) {
        logger.info("Creating new bus: {}", request.getBusNumber());

        // Vérifier que le numéro de bus n'existe pas déjà
        if (busRepository.existsByBusNumber(request.getBusNumber())) {
            throw new IllegalArgumentException("Bus number already exists: " + request.getBusNumber());
        }

        // Vérifier que la plaque d'immatriculation n'existe pas déjà
        if (busRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new IllegalArgumentException("License plate already exists: " + request.getLicensePlate());
        }

        // Créer le bus
        Bus bus = new Bus();
        bus.setBusNumber(request.getBusNumber());
        bus.setLicensePlate(request.getLicensePlate());
        bus.setType(request.getType());
        bus.setCapacity(request.getCapacity());

        Bus savedBus = busRepository.save(bus);
        logger.info("Bus created successfully: {}", savedBus.getId());

        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Récupérer tous les bus
     */
    public List<BusResponse> getAllBuses() {
        logger.info("Fetching all buses");
        return busRepository.findAll().stream()
                .map(BusResponse::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer un bus par ID
     */
    public BusResponse getBusById(String id) {
        logger.info("Fetching bus: {}", id);
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + id));
        return BusResponse.fromDomain(bus);
    }

    /**
     * Récupérer un bus par numéro
     */
    public BusResponse getBusByNumber(String busNumber) {
        logger.info("Fetching bus by number: {}", busNumber);
        Bus bus = busRepository.findByBusNumber(busNumber)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + busNumber));
        return BusResponse.fromDomain(bus);
    }

    /**
     * Récupérer les bus par statut
     */
    public List<BusResponse> getBusesByStatus(BusStatus status) {
        logger.info("Fetching buses with status: {}", status);
        return busRepository.findByStatus(status).stream()
                .map(BusResponse::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer les bus d'une ligne
     */
    public List<BusResponse> getBusesByRoute(UUID routeId) {
        logger.info("Fetching buses for route: {}", routeId);
        return busRepository.findByRouteId(routeId).stream()
                .map(BusResponse::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Assigner une ligne à un bus
     */
    public BusResponse assignRoute(AssignRouteRequest request) {
        logger.info("Assigning route {} to bus {}", request.getRouteNumber(), request.getBusId());

        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + request.getBusId()));

        bus.assignRoute(request.getRouteId(), request.getRouteNumber());
        Bus savedBus = busRepository.save(bus);

        logger.info("Route assigned successfully to bus {}", savedBus.getId());
        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Assigner un conducteur à un bus
     */
    public BusResponse assignDriver(String busId, UUID driverId, String driverName) {
        logger.info("Assigning driver {} to bus {}", driverName, busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + busId));

        bus.assignDriver(driverId, driverName);
        Bus savedBus = busRepository.save(bus);

        logger.info("Driver assigned successfully to bus {}", savedBus.getId());
        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Démarrer le service d'un bus
     */
    public BusResponse startService(String busId) {
        logger.info("Starting service for bus: {}", busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + busId));

        bus.startService();
        Bus savedBus = busRepository.save(bus);

        logger.info("Service started for bus {}", savedBus.getId());
        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Terminer le service d'un bus
     */
    public BusResponse endService(String busId) {
        logger.info("Ending service for bus: {}", busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + busId));

        bus.endService();
        Bus savedBus = busRepository.save(bus);

        logger.info("Service ended for bus {}", savedBus.getId());
        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Mettre un bus en maintenance
     */
    public BusResponse setMaintenance(String busId) {
        logger.info("Setting bus {} to maintenance", busId);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + busId));

        bus.setMaintenance();
        bus.setLastMaintenanceDate(LocalDateTime.now());
        Bus savedBus = busRepository.save(bus);

        logger.info("Bus {} set to maintenance", savedBus.getId());
        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Mettre à jour le nombre de passagers
     */
    public BusResponse updatePassengerCount(String busId, int count) {
        logger.info("Updating passenger count for bus {}: {}", busId, count);

        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + busId));

        bus.updatePassengerCount(count);
        Bus savedBus = busRepository.save(bus);

        logger.info("Passenger count updated for bus {}", savedBus.getId());
        return BusResponse.fromDomain(savedBus);
    }

    /**
     * Supprimer un bus
     */
    public void deleteBus(String busId) {
        logger.info("Deleting bus: {}", busId);

        if (!busRepository.existsById(busId)) {
            throw new IllegalArgumentException("Bus not found: " + busId);
        }

        busRepository.deleteById(busId);
        logger.info("Bus deleted: {}", busId);
    }

    /**
     * Obtenir des statistiques sur les bus
     */
    public BusStatistics getStatistics() {
        logger.info("Fetching bus statistics");

        long total = busRepository.count();
        long inService = busRepository.countByStatus(BusStatus.IN_SERVICE);
        long inactive = busRepository.countByStatus(BusStatus.INACTIVE);
        long maintenance = busRepository.countByStatus(BusStatus.MAINTENANCE);

        return new BusStatistics(total, inService, inactive, maintenance);
    }

    // Inner class pour les statistiques
    public static class BusStatistics {
        private long total;
        private long inService;
        private long inactive;
        private long maintenance;

        public BusStatistics(long total, long inService, long inactive, long maintenance) {
            this.total = total;
            this.inService = inService;
            this.inactive = inactive;
            this.maintenance = maintenance;
        }

        // Getters
        public long getTotal() { return total; }
        public long getInService() { return inService; }
        public long getInactive() { return inactive; }
        public long getMaintenance() { return maintenance; }
    }
}