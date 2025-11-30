package com.transport.notification.consumer;

import com.transport.notification.dto.BusEventNotification;
import com.transport.notification.dto.BusEventType;
import com.transport.notification.model.NotificationHistory;
import com.transport.notification.repository.NotificationHistoryRepository;
import com.transport.notification.service.EmailService;
import com.transport.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumer Kafka pour les événements de bus
 *
 * EXPLICATION DU CONSUMER :
 *
 * @KafkaListener : annotation qui indique qu'une méthode doit écouter un topic Kafka
 *
 * Fonctionnement :
 * 1. Le service de géolocalisation détecte un retard/annulation
 * 2. Il envoie un message JSON dans le topic Kafka
 * 3. Ce Consumer reçoit automatiquement le message
 * 4. Il traite le message (envoie email/SMS)
 * 5. Il sauvegarde l'historique dans MongoDB
 *
 * C'est un système ASYNCHRONE : le service géolocalisation n'attend pas
 * que les notifications soient envoyées, il continue son travail.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusEventConsumer {

    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * Écoute les retards de bus sur le topic "bus-delays"
     *
     * @KafkaListener paramètres :
     * - topics : nom du topic à écouter
     * - groupId : groupe de consommateurs (pour load balancing)
     * - containerFactory : factory configurée dans KafkaConsumerConfig
     */
    @KafkaListener(
        topics = "bus-delays",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeDelayEvent(
            @Payload BusEventNotification event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("📨 [KAFKA CONSUMER] Received delay event from partition {} offset {}", partition, offset);
        log.info("   Bus: {} | Route: {} | Delay: {} minutes",
                event.getBusNumber(), event.getRouteNumber(), event.getDelayMinutes());

        processNotification(event, "DELAY");
    }

    /**
     * Écoute les annulations de bus sur le topic "bus-cancellations"
     */
    @KafkaListener(
        topics = "bus-cancellations",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeCancellationEvent(
            @Payload BusEventNotification event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("📨 [KAFKA CONSUMER] Received cancellation event from partition {} offset {}", partition, offset);
        log.info("   Bus: {} | Route: {} | Message: {}",
                event.getBusNumber(), event.getRouteNumber(), event.getMessage());

        processNotification(event, "CANCELLATION");
    }

    /**
     * Écoute les alertes générales sur le topic "bus-alerts"
     */
    @KafkaListener(
        topics = "bus-alerts",
        groupId = "notification-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeAlertEvent(
            @Payload BusEventNotification event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset
    ) {
        log.info("📨 [KAFKA CONSUMER] Received alert event from partition {} offset {}", partition, offset);
        log.info("   Type: {} | Bus: {} | Route: {}",
                event.getEventType(), event.getBusNumber(), event.getRouteNumber());

        processNotification(event, "ALERT");
    }

    /**
     * Traite une notification reçue via Kafka
     *
     * LOGIQUE DE TRAITEMENT :
     * 1. Détermine le type de notification à envoyer
     * 2. Envoie l'email et/ou SMS
     * 3. Sauvegarde l'historique dans MongoDB
     * 4. Gère les erreurs de manière robuste
     */
    private void processNotification(BusEventNotification event, String category) {
        try {
            log.info("🔔 Processing {} notification for event: {}", category, event.getEventId());

            // Construire le message de notification
            String subject = buildNotificationSubject(event);
            String message = buildNotificationMessage(event);

            // Pour la démo, on utilise un email et téléphone par défaut
            // Dans un vrai système, on interrogerait une base d'utilisateurs
            String demoEmail = "passenger@transport.com";
            String demoPhone = "+1234567890";

            // Déterminer si c'est urgent (pour SMS)
            boolean isUrgent = isUrgentEvent(event);

            // Envoi EMAIL (toujours)
            NotificationHistory emailHistory = sendEmailNotification(
                event, demoEmail, subject, message
            );

            // Envoi SMS (seulement si urgent)
            if (isUrgent) {
                NotificationHistory smsHistory = sendSmsNotification(
                    event, demoPhone, message
                );
            }

            log.info("✅ Notification processed successfully for event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("❌ Error processing notification for event {}: {}",
                    event.getEventId(), e.getMessage(), e);
            // Dans un vrai système, on pourrait réessayer ou envoyer dans une DLQ (Dead Letter Queue)
        }
    }

    /**
     * Envoie une notification par email
     */
    private NotificationHistory sendEmailNotification(
            BusEventNotification event,
            String email,
            String subject,
            String message
    ) {
        try {
            // Tentative d'envoi email
            emailService.sendEmail(email, subject, message);

            // Sauvegarder dans l'historique
            NotificationHistory history = NotificationHistory.builder()
                    .eventId(event.getEventId())
                    .notificationType("EMAIL")
                    .recipient(email)
                    .email(email)
                    .subject(subject)
                    .message(message)
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .busNumber(event.getBusNumber())
                    .routeNumber(event.getRouteNumber())
                    .eventType(event.getEventType() != null ? event.getEventType().toString() : "UNKNOWN")
                    .priority(event.getPriority())
                    .build();

            return notificationHistoryRepository.save(history);

        } catch (Exception e) {
            log.error("Failed to send email notification: {}", e.getMessage());

            // Sauvegarder l'échec
            NotificationHistory history = NotificationHistory.builder()
                    .eventId(event.getEventId())
                    .notificationType("EMAIL")
                    .recipient(email)
                    .email(email)
                    .subject(subject)
                    .message(message)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .sentAt(LocalDateTime.now())
                    .busNumber(event.getBusNumber())
                    .routeNumber(event.getRouteNumber())
                    .eventType(event.getEventType() != null ? event.getEventType().toString() : "UNKNOWN")
                    .build();

            return notificationHistoryRepository.save(history);
        }
    }

    /**
     * Envoie une notification par SMS
     */
    private NotificationHistory sendSmsNotification(
            BusEventNotification event,
            String phoneNumber,
            String message
    ) {
        try {
            // Limiter le message SMS à 160 caractères
            String smsMessage = message.length() > 160 ?
                    message.substring(0, 157) + "..." : message;

            smsService.sendSms(phoneNumber, smsMessage);

            // Sauvegarder dans l'historique
            NotificationHistory history = NotificationHistory.builder()
                    .eventId(event.getEventId())
                    .notificationType("SMS")
                    .recipient(phoneNumber)
                    .phoneNumber(phoneNumber)
                    .message(smsMessage)
                    .status("SENT")
                    .sentAt(LocalDateTime.now())
                    .busNumber(event.getBusNumber())
                    .routeNumber(event.getRouteNumber())
                    .eventType(event.getEventType() != null ? event.getEventType().toString() : "UNKNOWN")
                    .priority(event.getPriority())
                    .build();

            return notificationHistoryRepository.save(history);

        } catch (Exception e) {
            log.error("Failed to send SMS notification: {}", e.getMessage());

            NotificationHistory history = NotificationHistory.builder()
                    .eventId(event.getEventId())
                    .notificationType("SMS")
                    .recipient(phoneNumber)
                    .phoneNumber(phoneNumber)
                    .message(message)
                    .status("FAILED")
                    .errorMessage(e.getMessage())
                    .sentAt(LocalDateTime.now())
                    .busNumber(event.getBusNumber())
                    .routeNumber(event.getRouteNumber())
                    .eventType(event.getEventType() != null ? event.getEventType().toString() : "UNKNOWN")
                    .build();

            return notificationHistoryRepository.save(history);
        }
    }

    /**
     * Construit le sujet de la notification
     */
    private String buildNotificationSubject(BusEventNotification event) {
        if (event.getTitle() != null && !event.getTitle().isEmpty()) {
            return event.getTitle();
        }

        return switch (event.getEventType()) {
            case DELAY -> String.format("⏰ Retard sur la ligne %s", event.getRouteNumber());
            case CANCELLATION -> String.format("❌ Annulation de la ligne %s", event.getRouteNumber());
            case BREAKDOWN -> String.format("🔧 Panne du bus %s", event.getBusNumber());
            case ROUTE_CHANGE -> String.format("🔄 Changement d'itinéraire - Ligne %s", event.getRouteNumber());
            default -> String.format("📢 Alerte ligne %s", event.getRouteNumber());
        };
    }

    /**
     * Construit le message de la notification
     */
    private String buildNotificationMessage(BusEventNotification event) {
        if (event.getMessage() != null && !event.getMessage().isEmpty()) {
            return event.getMessage();
        }

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("Bus %s - Ligne %s\n\n", event.getBusNumber(), event.getRouteNumber()));

        if (event.getEventType() == BusEventType.DELAY && event.getDelayMinutes() != null) {
            msg.append(String.format("Retard estimé: %d minutes\n", event.getDelayMinutes()));
        }

        if (event.getLocation() != null) {
            msg.append(String.format("Position: %s\n", event.getLocation()));
        }

        msg.append(String.format("\nHeure: %s", event.getTimestamp()));

        return msg.toString();
    }

    /**
     * Détermine si l'événement est urgent (nécessite SMS)
     */
    private boolean isUrgentEvent(BusEventNotification event) {
        // Annulations et pannes = toujours urgent
        if (event.getEventType() == BusEventType.CANCELLATION ||
            event.getEventType() == BusEventType.BREAKDOWN) {
            return true;
        }

        // Retards > 15 minutes = urgent
        if (event.getEventType() == BusEventType.DELAY &&
            event.getDelayMinutes() != null &&
            event.getDelayMinutes() > 15) {
            return true;
        }

        // Priorité HIGH ou URGENT
        return "HIGH".equalsIgnoreCase(event.getPriority()) ||
               "URGENT".equalsIgnoreCase(event.getPriority());
    }
}
