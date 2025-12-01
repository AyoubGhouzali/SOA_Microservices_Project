package com.transport.notification.config;

import com.transport.notification.dto.BusEventNotification;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration Kafka Consumer
 *
 * EXPLICATION KAFKA POUR DÉBUTANTS :
 *
 * Un CONSUMER écoute des messages sur des TOPICS Kafka.
 * C'est comme s'abonner à une chaîne de notifications.
 *
 * Topics utilisés dans ce projet :
 * - "bus-delays" : pour les retards de bus
 * - "bus-cancellations" : pour les annulations
 * - "bus-alerts" : pour les alertes générales
 *
 * Le Consumer Group permet à plusieurs instances du service
 * de partager la charge de travail (scaling horizontal).
 */
@EnableKafka
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers:kafka:9093}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-service-group}")
    private String groupId;

    /**
     * Configuration du Consumer Kafka
     *
     * KEY_DESERIALIZER : convertit la clé du message (String)
     * VALUE_DESERIALIZER : convertit le corps du message (JSON vers BusEventNotification)
     * AUTO_OFFSET_RESET : "earliest" = lire depuis le début si pas de offset sauvegardé
     *                      "latest" = lire seulement les nouveaux messages
     */
    @Bean
    public ConsumerFactory<String, BusEventNotification> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        // Adresse du serveur Kafka
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        // Groupe de consommateurs (important pour le scaling)
        config.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);

        // Désérialisation de la clé (String)
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // Désérialisation de la valeur (JSON -> Object Java)
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Lire depuis le début si nouveau consumer
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // Configuration du JSON deserializer
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, BusEventNotification.class.getName());

        // Auto-commit des offsets (marquer les messages comme lus)
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        config.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 1000);

        return new DefaultKafkaConsumerFactory<>(
            config,
            new StringDeserializer(),
            new JsonDeserializer<>(BusEventNotification.class, false)
        );
    }

    /**
     * Factory pour créer des listeners Kafka
     * Permet de traiter plusieurs messages en parallèle (concurrency)
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, BusEventNotification>
            kafkaListenerContainerFactory() {

        ConcurrentKafkaListenerContainerFactory<String, BusEventNotification> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());

        // Nombre de threads pour traiter les messages en parallèle
        factory.setConcurrency(3);

        return factory;
    }
}
