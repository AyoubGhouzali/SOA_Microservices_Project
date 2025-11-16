package com.transport.tracking.service;

import com.transport.tracking.dto.LocationResponse;
import com.transport.tracking.dto.UpdateLocationRequest;
import com.transport.tracking.model.Bus;
import com.transport.tracking.model.BusLocation;
import com.transport.tracking.repository.BusLocationRepository;
import com.transport.tracking.repository.BusRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TrackingService {

    private static final Logger logger = LoggerFactory.getLogger(TrackingService.class);

    private final BusLocationRepository locationRepository;
    private final BusRepository busRepository;

    public TrackingService(BusLocationRepository locationRepository,
                           BusRepository busRepository) {
        this.locationRepository = locationRepository;
        this.busRepository = busRepository;
    }

    /**
     * Mettre à jour la position d'un bus
     */
    public LocationResponse updateLocation(UpdateLocationRequest request) {
        logger.info("Updating location for bus: {}", request.getBusId());

        // Vérifier que le bus existe
        Bus bus = busRepository.findById(request.getBusId())
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + request.getBusId()));

        // Créer une nouvelle position
        BusLocation location = new BusLocation();
        location.setBusId(request.getBusId());
        location.setBusNumber(bus.getBusNumber());
        location.setRouteNumber(bus.getRouteNumber());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setAltitude(request.getAltitude());
        location.setSpeed(request.getSpeed());
        location.setHeading(request.getHeading());
        location.setAccuracy(request.getAccuracy());
        location.setTimestamp(LocalDateTime.now());

        // Mettre à jour le nombre de passagers si fourni
        if (request.getCurrentPassengers() != null) {
            bus.updatePassengerCount(request.getCurrentPassengers());
            busRepository.save(bus);

            location.setCurrentPassengers(request.getCurrentPassengers());
            location.setOccupancyRate(bus.getOccupancyRate());
        }

        // Sauvegarder la position
        BusLocation savedLocation = locationRepository.save(location);
        logger.info("Location updated for bus {}: ({}, {})",
                request.getBusId(), request.getLatitude(), request.getLongitude());

        return LocationResponse.fromDomain(savedLocation);
    }

    /**
     * Obtenir la dernière position d'un bus
     */
    public LocationResponse getLatestLocation(String busId) {
        logger.info("Fetching latest location for bus: {}", busId);

        BusLocation location = locationRepository.findFirstByBusIdOrderByTimestampDesc(busId)
                .orElseThrow(() -> new IllegalArgumentException("No location found for bus: " + busId));

        return LocationResponse.fromDomain(location);
    }

    /**
     * Obtenir l'historique des positions d'un bus
     */
    public List<LocationResponse> getLocationHistory(String busId, LocalDateTime start, LocalDateTime end) {
        logger.info("Fetching location history for bus {} from {} to {}", busId, start, end);

        List<BusLocation> locations = locationRepository
                .findByBusIdAndTimestampBetweenOrderByTimestampDesc(busId, start, end);

        return locations.stream()
                .map(LocationResponse::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les positions de tous les bus d'une ligne
     */
    public List<LocationResponse> getLocationsByRoute(String routeNumber) {
        logger.info("Fetching locations for route: {}", routeNumber);

        List<BusLocation> locations = locationRepository.findByRouteNumberOrderByTimestampDesc(routeNumber);

        // Garder seulement la dernière position de chaque bus
        return locations.stream()
                .collect(Collectors.groupingBy(BusLocation::getBusId))
                .values().stream()
                .map(list -> list.get(0))  // Prendre la plus récente
                .map(LocationResponse::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir toutes les positions récentes (dernière heure)
     */
    public List<LocationResponse> getRecentLocations() {
        logger.info("Fetching all recent locations");

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        List<BusLocation> locations = locationRepository.findByTimestampAfterOrderByTimestampDesc(oneHourAgo);

        // Garder seulement la dernière position de chaque bus
        return locations.stream()
                .collect(Collectors.groupingBy(BusLocation::getBusId))
                .values().stream()
                .map(list -> list.get(0))
                .map(LocationResponse::fromDomain)
                .collect(Collectors.toList());
    }

    /**
     * Calculer la distance entre deux points GPS (formule de Haversine)
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Rayon de la Terre en km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance en km
    }

    /**
     * Trouver les bus à proximité d'un point
     */
    public List<LocationResponse> findNearbyBuses(double latitude, double longitude, double radiusKm) {
        logger.info("Finding buses near ({}, {}) within {} km", latitude, longitude, radiusKm);

        List<LocationResponse> recentLocations = getRecentLocations();

        return recentLocations.stream()
                .filter(location -> {
                    double distance = calculateDistance(
                            latitude, longitude,
                            location.getLatitude(), location.getLongitude()
                    );
                    return distance <= radiusKm;
                })
                .collect(Collectors.toList());
    }

    /**
     * Nettoyer les anciennes positions (plus de 7 jours)
     */
    public void cleanOldLocations() {
        logger.info("Cleaning old locations");
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        locationRepository.deleteByTimestampBefore(sevenDaysAgo);
        logger.info("Old locations cleaned");
    }
}