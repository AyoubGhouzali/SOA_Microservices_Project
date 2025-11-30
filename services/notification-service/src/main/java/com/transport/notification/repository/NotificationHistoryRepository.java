package com.transport.notification.repository;

import com.transport.notification.model.NotificationHistory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository pour l'historique des notifications
 */
@Repository
public interface NotificationHistoryRepository extends MongoRepository<NotificationHistory, String> {

    // Trouver toutes les notifications pour un événement
    List<NotificationHistory> findByEventId(String eventId);

    // Trouver les notifications par statut
    List<NotificationHistory> findByStatus(String status);

    // Trouver les notifications par destinataire
    List<NotificationHistory> findByRecipient(String recipient);

    // Trouver les notifications dans une période
    List<NotificationHistory> findBySentAtBetween(LocalDateTime start, LocalDateTime end);

    // Trouver les notifications échouées
    List<NotificationHistory> findByStatusAndSentAtAfter(String status, LocalDateTime after);
}
