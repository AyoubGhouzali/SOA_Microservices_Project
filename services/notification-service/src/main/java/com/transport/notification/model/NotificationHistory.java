package com.transport.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Historique des notifications envoyées
 * Stocké dans MongoDB pour tracking et analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_history")
public class NotificationHistory {

    @Id
    private String id;

    // Référence à l'événement source
    @Indexed
    private String eventId;

    // Type de notification (EMAIL, SMS)
    private String notificationType;

    // Destinataire
    private String recipient;
    private String email;
    private String phoneNumber;

    // Contenu
    private String subject;
    private String message;

    // Statut (SENT, FAILED, PENDING)
    @Indexed
    private String status;

    // Détails de l'erreur (si échec)
    private String errorMessage;

    // Timestamps
    @Indexed
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;

    // Métadonnées de l'événement bus
    private String busNumber;
    private String routeNumber;
    private String eventType;

    // Pour analytics
    private String priority;
}
