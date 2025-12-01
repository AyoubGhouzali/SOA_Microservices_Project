package com.transport.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Notification Service - Service de notifications
 *
 * Ce service :
 * - Écoute les événements Kafka (retards, annulations)
 * - Envoie des notifications par email et SMS
 * - Stocke l'historique dans MongoDB
 * - S'enregistre dans Eureka pour la découverte de services
 *
 * Architecture:
 * - Kafka Consumer : reçoit les événements du service de géolocalisation
 * - Email Service : envoie des emails via SMTP
 * - SMS Service : envoie des SMS via Twilio
 * - MongoDB : stocke l'historique des notifications
 */
@SpringBootApplication
@EnableDiscoveryClient  // S'enregistre dans Eureka
@EnableKafka           // Active le support Kafka
public class NotificationApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationApplication.class, args);
    }
}
