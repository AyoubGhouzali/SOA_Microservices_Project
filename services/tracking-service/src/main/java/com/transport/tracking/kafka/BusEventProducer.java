package com.transport.tracking.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Service Producer Kafka pour les événements de bus
 *
 * RÔLE DU PRODUCER :
 *
 * Ce service envoie des événements vers Kafka quand :
 * - Un bus a du retard
 * - Un trajet est annulé
 * - Il y a une panne ou un changement d'itinéraire
 *
 * FLUX DE COMMUNICATION :
 * Tracking Service -> Kafka -> Notification Service -> Email/SMS
 *
 * C'est une communication ASYNCHRONE : le tracking service n'attend pas
 * que les notifications soient envoyées pour continuer à fonctionner.
 */
@Service
public class BusEventProducer {

    private static final Logger logger = LoggerFactory.getLogger(BusEventProducer.class);

    // Topics Kafka (doivent correspondre à ceux du Consumer)
    private static final String TOPIC_DELAYS = "bus-delays";
    private static final String TOPIC_CANCELLATIONS = "bus-cancellations";
    private static final String TOPIC_ALERTS = "bus-alerts";

    private final KafkaTemplate<String, BusEventNotification> kafkaTemplate;

    public BusEventProducer(KafkaTemplate<String, BusEventNotification> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publier un événement de retard
     *
     * @param busId ID du bus
     * @param busNumber Numéro du bus
     * @param routeNumber Ligne
     * @param delayMinutes Retard en minutes
     * @param latitude Position GPS
     * @param longitude Position GPS
     * @param message Message descriptif
     */
    public void publishDelayEvent(
            String busId,
            String busNumber,
            String routeNumber,
            int delayMinutes,
            Double latitude,
            Double longitude,
            String message
    ) {
        logger.info("📤 Publishing DELAY event for bus {} (Route {}): {} minutes",
                busNumber, routeNumber, delayMinutes);

        BusEventNotification event = BusEventNotification.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BusEventType.DELAY)
                .busId(busId)
                .busNumber(busNumber)
                .routeNumber(routeNumber)
                .delayMinutes(delayMinutes)
                .latitude(latitude)
                .longitude(longitude)
                .title(String.format("Retard sur la ligne %s", routeNumber))
                .message(message != null ? message :
                        String.format("Le bus %s a un retard de %d minutes", busNumber, delayMinutes))
                .priority(delayMinutes > 15 ? "HIGH" : "MEDIUM")
                .timestamp(LocalDateTime.now())
                .affectedUsers("ALL") // Notifier tous les utilisateurs de cette ligne
                .build();

        sendToKafka(TOPIC_DELAYS, event);
    }

    /**
     * Publier un événement d'annulation
     */
    public void publishCancellationEvent(
            String busId,
            String busNumber,
            String routeNumber,
            String reason,
            Double latitude,
            Double longitude
    ) {
        logger.info("📤 Publishing CANCELLATION event for bus {} (Route {})", busNumber, routeNumber);

        BusEventNotification event = BusEventNotification.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BusEventType.CANCELLATION)
                .busId(busId)
                .busNumber(busNumber)
                .routeNumber(routeNumber)
                .latitude(latitude)
                .longitude(longitude)
                .title(String.format("Annulation de la ligne %s", routeNumber))
                .message(reason != null ? reason :
                        String.format("Le trajet du bus %s a été annulé", busNumber))
                .priority("URGENT")
                .timestamp(LocalDateTime.now())
                .affectedUsers("ALL")
                .build();

        sendToKafka(TOPIC_CANCELLATIONS, event);
    }

    /**
     * Publier un événement de panne
     */
    public void publishBreakdownEvent(
            String busId,
            String busNumber,
            String routeNumber,
            String description,
            Double latitude,
            Double longitude
    ) {
        logger.info("📤 Publishing BREAKDOWN event for bus {} (Route {})", busNumber, routeNumber);

        BusEventNotification event = BusEventNotification.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BusEventType.BREAKDOWN)
                .busId(busId)
                .busNumber(busNumber)
                .routeNumber(routeNumber)
                .latitude(latitude)
                .longitude(longitude)
                .title(String.format("Panne du bus %s", busNumber))
                .message(description != null ? description :
                        String.format("Le bus %s est en panne", busNumber))
                .priority("URGENT")
                .timestamp(LocalDateTime.now())
                .affectedUsers("ALL")
                .build();

        sendToKafka(TOPIC_ALERTS, event);
    }

    /**
     * Publier un événement de changement d'itinéraire
     */
    public void publishRouteChangeEvent(
            String busId,
            String busNumber,
            String routeNumber,
            String newRoute,
            String reason,
            Double latitude,
            Double longitude
    ) {
        logger.info("📤 Publishing ROUTE_CHANGE event for bus {} (Route {} -> {})",
                busNumber, routeNumber, newRoute);

        BusEventNotification event = BusEventNotification.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BusEventType.ROUTE_CHANGE)
                .busId(busId)
                .busNumber(busNumber)
                .routeNumber(routeNumber)
                .latitude(latitude)
                .longitude(longitude)
                .title(String.format("Changement d'itinéraire - Ligne %s", routeNumber))
                .message(reason != null ? reason :
                        String.format("Le bus %s change d'itinéraire", busNumber))
                .priority("HIGH")
                .timestamp(LocalDateTime.now())
                .affectedUsers("ALL")
                .metadata(String.format("new_route=%s", newRoute))
                .build();

        sendToKafka(TOPIC_ALERTS, event);
    }

    /**
     * Publier une alerte de trafic
     */
    public void publishTrafficAlert(
            String routeNumber,
            String location,
            String description,
            Double latitude,
            Double longitude
    ) {
        logger.info("📤 Publishing TRAFFIC_ALERT for route {}", routeNumber);

        BusEventNotification event = BusEventNotification.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(BusEventType.TRAFFIC_ALERT)
                .routeNumber(routeNumber)
                .latitude(latitude)
                .longitude(longitude)
                .location(location)
                .title(String.format("Alerte trafic - Ligne %s", routeNumber))
                .message(description)
                .priority("MEDIUM")
                .timestamp(LocalDateTime.now())
                .affectedUsers("ALL")
                .build();

        sendToKafka(TOPIC_ALERTS, event);
    }

    /**
     * Envoie un message à Kafka
     *
     * IMPORTANT : Cette méthode est ASYNCHRONE
     * Elle ne bloque pas le thread appelant
     */
    private void sendToKafka(String topic, BusEventNotification event) {
        try {
            // La clé est l'ID du bus (pour partitionnement)
            String key = event.getBusId() != null ? event.getBusId() : event.getRouteNumber();

            // Envoi asynchrone vers Kafka
            CompletableFuture<SendResult<String, BusEventNotification>> future =
                    kafkaTemplate.send(topic, key, event);

            // Callback pour savoir si l'envoi a réussi
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("✅ Event sent successfully to topic '{}' | Event ID: {} | Partition: {} | Offset: {}",
                            topic,
                            event.getEventId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    logger.error("❌ Failed to send event to topic '{}' | Event ID: {} | Error: {}",
                            topic, event.getEventId(), ex.getMessage(), ex);
                }
            });

        } catch (Exception e) {
            logger.error("❌ Exception while sending to Kafka: {}", e.getMessage(), e);
        }
    }

    /**
     * Méthode générique pour publier un événement personnalisé
     */
    public void publishCustomEvent(BusEventNotification event) {
        String topic = switch (event.getEventType()) {
            case DELAY -> TOPIC_DELAYS;
            case CANCELLATION -> TOPIC_CANCELLATIONS;
            default -> TOPIC_ALERTS;
        };

        sendToKafka(topic, event);
    }
}
