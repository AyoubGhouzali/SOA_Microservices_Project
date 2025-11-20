package com.transport.tracking.controller;

import com.transport.tracking.kafka.BusEventProducer;
import com.transport.tracking.model.Bus;
import com.transport.tracking.model.BusLocation;
import com.transport.tracking.repository.BusRepository;
import com.transport.tracking.repository.BusLocationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller pour envoyer des événements de bus vers Kafka
 *
 * Endpoints pour tester les notifications :
 * - POST /api/bus-events/delay : Signaler un retard
 * - POST /api/bus-events/cancellation : Signaler une annulation
 * - POST /api/bus-events/breakdown : Signaler une panne
 */
@RestController
@RequestMapping("/api/bus-events")
public class BusEventController {

    private static final Logger logger = LoggerFactory.getLogger(BusEventController.class);

    private final BusEventProducer busEventProducer;
    private final BusRepository busRepository;
    private final BusLocationRepository locationRepository;

    public BusEventController(
            BusEventProducer busEventProducer,
            BusRepository busRepository,
            BusLocationRepository locationRepository
    ) {
        this.busEventProducer = busEventProducer;
        this.busRepository = busRepository;
        this.locationRepository = locationRepository;
    }

    /**
     * Signaler un retard de bus
     * POST /api/bus-events/delay
     *
     * Body JSON:
     * {
     *   "busId": "bus-001",
     *   "delayMinutes": 20,
     *   "message": "Retard dû à un accident sur l'autoroute"
     * }
     */
    @PostMapping("/delay")
    public ResponseEntity<Map<String, Object>> reportDelay(@RequestBody Map<String, Object> request) {
        try {
            String busId = (String) request.get("busId");
            Integer delayMinutes = (Integer) request.get("delayMinutes");
            String message = (String) request.get("message");

            // Récupérer les infos du bus
            Optional<Bus> busOpt = busRepository.findById(busId);
            if (busOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Bus not found: " + busId
                ));
            }

            Bus bus = busOpt.get();

            // Récupérer la dernière position
            Optional<BusLocation> locationOpt = locationRepository
                    .findFirstByBusIdOrderByTimestampDesc(busId);

            Double latitude = locationOpt.map(BusLocation::getLatitude).orElse(null);
            Double longitude = locationOpt.map(BusLocation::getLongitude).orElse(null);

            // Envoyer l'événement vers Kafka
            busEventProducer.publishDelayEvent(
                busId,
                bus.getBusNumber(),
                bus.getRouteNumber(),
                delayMinutes,
                latitude,
                longitude,
                message
            );

            logger.info("Delay event sent for bus {} - {} minutes", bus.getBusNumber(), delayMinutes);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Delay event published to Kafka",
                "busNumber", bus.getBusNumber(),
                "routeNumber", bus.getRouteNumber(),
                "delayMinutes", delayMinutes
            ));

        } catch (Exception e) {
            logger.error("Error reporting delay: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Signaler une annulation de trajet
     * POST /api/bus-events/cancellation
     */
    @PostMapping("/cancellation")
    public ResponseEntity<Map<String, Object>> reportCancellation(@RequestBody Map<String, Object> request) {
        try {
            String busId = (String) request.get("busId");
            String reason = (String) request.get("reason");

            Optional<Bus> busOpt = busRepository.findById(busId);
            if (busOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Bus not found: " + busId
                ));
            }

            Bus bus = busOpt.get();

            Optional<BusLocation> locationOpt = locationRepository
                    .findFirstByBusIdOrderByTimestampDesc(busId);

            Double latitude = locationOpt.map(BusLocation::getLatitude).orElse(null);
            Double longitude = locationOpt.map(BusLocation::getLongitude).orElse(null);

            busEventProducer.publishCancellationEvent(
                busId,
                bus.getBusNumber(),
                bus.getRouteNumber(),
                reason,
                latitude,
                longitude
            );

            logger.info("Cancellation event sent for bus {}", bus.getBusNumber());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Cancellation event published to Kafka",
                "busNumber", bus.getBusNumber(),
                "routeNumber", bus.getRouteNumber()
            ));

        } catch (Exception e) {
            logger.error("Error reporting cancellation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Signaler une panne
     * POST /api/bus-events/breakdown
     */
    @PostMapping("/breakdown")
    public ResponseEntity<Map<String, Object>> reportBreakdown(@RequestBody Map<String, Object> request) {
        try {
            String busId = (String) request.get("busId");
            String description = (String) request.get("description");

            Optional<Bus> busOpt = busRepository.findById(busId);
            if (busOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Bus not found: " + busId
                ));
            }

            Bus bus = busOpt.get();

            Optional<BusLocation> locationOpt = locationRepository
                    .findFirstByBusIdOrderByTimestampDesc(busId);

            Double latitude = locationOpt.map(BusLocation::getLatitude).orElse(null);
            Double longitude = locationOpt.map(BusLocation::getLongitude).orElse(null);

            busEventProducer.publishBreakdownEvent(
                busId,
                bus.getBusNumber(),
                bus.getRouteNumber(),
                description,
                latitude,
                longitude
            );

            logger.info("Breakdown event sent for bus {}", bus.getBusNumber());

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Breakdown event published to Kafka",
                "busNumber", bus.getBusNumber(),
                "routeNumber", bus.getRouteNumber()
            ));

        } catch (Exception e) {
            logger.error("Error reporting breakdown: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Signaler une alerte de trafic
     * POST /api/bus-events/traffic-alert
     */
    @PostMapping("/traffic-alert")
    public ResponseEntity<Map<String, Object>> reportTrafficAlert(@RequestBody Map<String, Object> request) {
        try {
            String routeNumber = (String) request.get("routeNumber");
            String location = (String) request.get("location");
            String description = (String) request.get("description");
            Double latitude = request.get("latitude") != null ?
                    ((Number) request.get("latitude")).doubleValue() : null;
            Double longitude = request.get("longitude") != null ?
                    ((Number) request.get("longitude")).doubleValue() : null;

            busEventProducer.publishTrafficAlert(
                routeNumber,
                location,
                description,
                latitude,
                longitude
            );

            logger.info("Traffic alert sent for route {}", routeNumber);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Traffic alert published to Kafka",
                "routeNumber", routeNumber
            ));

        } catch (Exception e) {
            logger.error("Error reporting traffic alert: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }
}
