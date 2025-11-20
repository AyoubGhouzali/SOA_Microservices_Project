package com.transport.notification.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration des Topics Kafka
 *
 * EXPLICATION TOPICS :
 *
 * Un TOPIC est un canal de communication dans Kafka.
 * C'est comme une file d'attente où les messages sont stockés.
 *
 * - Les PRODUCERS envoient des messages dans un topic
 * - Les CONSUMERS lisent les messages depuis un topic
 *
 * Chaque topic peut avoir plusieurs PARTITIONS pour la scalabilité
 * et plusieurs REPLICAS pour la haute disponibilité.
 */
@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka:9093}")
    private String bootstrapServers;

    /**
     * Configuration de l'admin Kafka
     * Permet de créer/gérer les topics
     */
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return new KafkaAdmin(configs);
    }

    /**
     * Topic pour les retards de bus
     *
     * Paramètres :
     * - Nom : "bus-delays"
     * - Partitions : 3 (permet de paralléliser le traitement)
     * - Réplication : 1 (nombre de copies - 1 car un seul broker Kafka)
     */
    @Bean
    public NewTopic busDelaysTopic() {
        return new NewTopic("bus-delays", 3, (short) 1);
    }

    /**
     * Topic pour les annulations de bus
     */
    @Bean
    public NewTopic busCancellationsTopic() {
        return new NewTopic("bus-cancellations", 3, (short) 1);
    }

    /**
     * Topic pour les alertes générales
     * (pannes, changements d'itinéraire, etc.)
     */
    @Bean
    public NewTopic busAlertsTopic() {
        return new NewTopic("bus-alerts", 3, (short) 1);
    }

    /**
     * Topic pour les mises à jour de position en temps réel
     * (optionnel - pour streaming de positions)
     */
    @Bean
    public NewTopic busLocationUpdatesTopic() {
        return new NewTopic("bus-location-updates", 5, (short) 1);
    }
}
