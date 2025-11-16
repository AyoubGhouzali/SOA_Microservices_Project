package com.transport.scheduling.repository;

import com.transport.scheduling.model.Schedule;
import com.transport.scheduling.model.ScheduleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;

/**
 * Repository pour Schedule
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, UUID> {

    List<Schedule> findByRouteId(UUID routeId);

    List<Schedule> findByRouteIdAndType(UUID routeId, ScheduleType type);

    List<Schedule> findByIsActiveTrue();

    @Query("SELECT s FROM Schedule s JOIN s.activeDays d WHERE s.routeId = :routeId AND d = :day AND s.isActive = true")
    List<Schedule> findActiveSchedulesByRouteIdAndDay(
            @Param("routeId") UUID routeId,
            @Param("day") DayOfWeek day
    );
}