package com.transport.scheduling.repository;

import com.transport.scheduling.model.Route;
import com.transport.scheduling.model.RouteStatus;
import com.transport.scheduling.model.RouteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour Route
 */
@Repository
public interface RouteRepository extends JpaRepository<Route, UUID> {

    Optional<Route> findByRouteNumber(String routeNumber);

    List<Route> findByStatus(RouteStatus status);

    List<Route> findByType(RouteType type);

    boolean existsByRouteNumber(String routeNumber);
}