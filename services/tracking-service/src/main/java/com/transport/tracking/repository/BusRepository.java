package com.transport.tracking.repository;

import com.transport.tracking.model.Bus;
import com.transport.tracking.model.BusStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BusRepository extends MongoRepository<Bus, String> {

    Optional<Bus> findByBusNumber(String busNumber);

    List<Bus> findByStatus(BusStatus status);

    List<Bus> findByRouteId(UUID routeId);

    List<Bus> findByRouteNumber(String routeNumber);

    List<Bus> findByDriverId(UUID driverId);

    boolean existsByBusNumber(String busNumber);

    boolean existsByLicensePlate(String licensePlate);

    long countByStatus(BusStatus status);
}