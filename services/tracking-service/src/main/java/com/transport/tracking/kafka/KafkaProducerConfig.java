package com.transport.tracking.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka Producer
 *
 * EXPLICATION PRODUCER :
 *
 * Un PRODUCER envoie des messages vers des TOPICS Kafka.
 * C'est comme publier des notifications sur un canal.
 *
 * Le Producer sérialise les objets Java en JSON et les envoie à Kafka.
 * Le Consumer (dans le service de notifications) les reçoit et les traite.
 *
 * Architecture :
 * 1. Le service de tracking détecte un retard
 * 2. Il crée un BusEventNotification
 * 3. Il l'envoie dans le topic "bus-delays"
 * 4. Kafka stocke le message
 * 5. Le service de notifications le reçoit automatiquement
 * 6. Le service de notifications envoie l'email/SMS
 */
@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka:9093}")
    private String bootstrapServers;

    /**
     * Configuration du Producer Kafka
     *
     * KEY_SERIALIZER : convertit la clé (String) en bytes
     * VALUE_SERIALIZER : convertit l'objet Java en JSON
     */
    @Bean
    public ProducerFactory<String, BusEventNotification> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Adresse du serveur Kafka
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Sérialisation de la clé (String)
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        // Sérialisation de la valeur (Object -> JSON)
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Configuration pour la fiabilité
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Attendre confirmation de tous les brokers
        config.put(ProducerConfig.RETRIES_CONFIG, 3);  // Réessayer 3 fois en cas d'échec
        config.put(ProducerConfig.LINGER_MS_CONFIG, 1); // Attendre 1ms avant d'envoyer (batching)

        // Configuration du JSON serializer
        config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * KafkaTemplate : classe utilitaire pour envoyer des messages
     * C'est l'interface principale pour le Producer
     */
    @Bean
    public KafkaTemplate<String, BusEventNotification> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
