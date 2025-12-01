package com.transport.notification.controller;

import com.transport.notification.dto.NotificationRequest;
import com.transport.notification.model.NotificationHistory;
import com.transport.notification.repository.NotificationHistoryRepository;
import com.transport.notification.service.EmailService;
import com.transport.notification.service.SmsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller pour les notifications
 *
 * Endpoints pour :
 * - Envoyer des notifications manuelles (test)
 * - Consulter l'historique des notifications
 * - Obtenir des statistiques
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final SmsService smsService;
    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "notification-service",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Envoyer une notification manuelle (pour test)
     * POST /api/notifications/send
     */
    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendNotification(
            @Valid @RequestBody NotificationRequest request
    ) {
        try {
            log.info("Received manual notification request for: {}", request.getRecipient());

            String notifType = request.getNotificationType() != null ?
                    request.getNotificationType().toUpperCase() : "EMAIL";

            // Envoyer email
            if (notifType.equals("EMAIL") || notifType.equals("BOTH")) {
                if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                    emailService.sendEmail(
                        request.getEmail(),
                        request.getSubject(),
                        request.getMessage()
                    );
                }
            }

            // Envoyer SMS
            if (notifType.equals("SMS") || notifType.equals("BOTH")) {
                if (request.getPhoneNumber() != null && !request.getPhoneNumber().isEmpty()) {
                    smsService.sendSms(
                        request.getPhoneNumber(),
                        request.getMessage()
                    );
                }
            }

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Notification sent successfully",
                "recipient", request.getRecipient(),
                "type", notifType
            ));

        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Récupérer l'historique des notifications
     * GET /api/notifications/history
     */
    @GetMapping("/history")
    public ResponseEntity<List<NotificationHistory>> getHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventId
    ) {
        try {
            List<NotificationHistory> history;

            if (eventId != null) {
                history = notificationHistoryRepository.findByEventId(eventId);
            } else if (status != null) {
                history = notificationHistoryRepository.findByStatus(status);
            } else {
                history = notificationHistoryRepository.findAll();
            }

            return ResponseEntity.ok(history);

        } catch (Exception e) {
            log.error("Error fetching notification history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer les statistiques des notifications
     * GET /api/notifications/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        try {
            long total = notificationHistoryRepository.count();
            long sent = notificationHistoryRepository.findByStatus("SENT").size();
            long failed = notificationHistoryRepository.findByStatus("FAILED").size();

            return ResponseEntity.ok(Map.of(
                "total", total,
                "sent", sent,
                "failed", failed,
                "successRate", total > 0 ? (double) sent / total * 100 : 0
            ));

        } catch (Exception e) {
            log.error("Error fetching stats: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupérer les notifications récentes
     * GET /api/notifications/recent
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NotificationHistory>> getRecentNotifications(
            @RequestParam(defaultValue = "10") int limit
    ) {
        try {
            List<NotificationHistory> notifications = notificationHistoryRepository.findAll();

            // Trier par date décroissante et limiter
            List<NotificationHistory> recent = notifications.stream()
                .sorted((a, b) -> b.getSentAt().compareTo(a.getSentAt()))
                .limit(limit)
                .toList();

            return ResponseEntity.ok(recent);

        } catch (Exception e) {
            log.error("Error fetching recent notifications: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
