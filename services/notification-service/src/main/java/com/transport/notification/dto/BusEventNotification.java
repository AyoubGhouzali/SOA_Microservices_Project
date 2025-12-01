package com.transport.notification.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO pour les événements de bus envoyés via Kafka
 * Ce format sera utilisé par le service de géolocalisation (Producer)
 * et reçu par le service de notifications (Consumer)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusEventNotification {

    // Identifiant unique de l'événement
    private String eventId;

    // Type d'événement (DELAY, CANCELLATION, etc.)
    private BusEventType eventType;

    // Informations du bus
    private String busId;
    private String busNumber;
    private String routeNumber;

    // Détails de l'événement
    private String title;           // Ex: "Retard sur la ligne 42"
    private String message;         // Description détaillée

    // Pour les retards : durée en minutes
    private Integer delayMinutes;

    // Localisation
    private Double latitude;
    private Double longitude;
    private String location;        // Description textuelle

    // Timestamp de l'événement
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    // Priorité (LOW, MEDIUM, HIGH, URGENT)
    private String priority;

    // Liste des utilisateurs à notifier (IDs séparés par virgules)
    // ou "ALL" pour notifier tous les utilisateurs de cette ligne
    private String affectedUsers;

    // Métadonnées additionnelles
    private String metadata;
}
