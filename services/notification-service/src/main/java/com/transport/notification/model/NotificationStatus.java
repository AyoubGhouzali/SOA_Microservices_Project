package com.transport.notification.model;

/**
 * Statuts possibles d'une notification
 */
public enum NotificationStatus {
    PENDING,    // En attente d'envoi
    SENT,       // Envoyée avec succès
    DELIVERED,  // Délivrée (confirmation reçue)
    FAILED,     // Échec d'envoi
    BOUNCED     // Rejetée par le serveur
}
