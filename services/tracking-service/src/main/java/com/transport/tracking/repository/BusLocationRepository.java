package com.transport.tracking.repository;

import com.transport.tracking.model.BusLocation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusLocationRepository extends MongoRepository<BusLocation, String> {

    // Trouver la dernière position d'un bus
    Optional<BusLocation> findFirstByBusIdOrderByTimestampDesc(String busId);

    // Trouver toutes les positions d'un bus dans une période
    List<BusLocation> findByBusIdAndTimestampBetweenOrderByTimestampDesc(
            String busId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Trouver les dernières positions de tous les bus d'une ligne
    List<BusLocation> findByRouteNumberOrderByTimestampDesc(String routeNumber);

    // Trouver toutes les positions récentes (dernière heure)
    List<BusLocation> findByTimestampAfterOrderByTimestampDesc(LocalDateTime since);

    // Supprimer les anciennes positions
    void deleteByTimestampBefore(LocalDateTime before);
}