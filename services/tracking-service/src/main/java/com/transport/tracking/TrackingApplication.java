package com.transport.tracking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

/**
 * Tracking Service - Service de géolocalisation des bus
 *
 * Ce service :
 * - Suit la position GPS des bus en temps réel
 * - Détecte les retards et anomalies
 * - Envoie des événements vers Kafka (Producer)
 * - Communique avec le service de notifications
 */
@SpringBootApplication
@EnableKafka  // Active le support Kafka (Producer)
public class TrackingApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackingApplication.class, args);
	}

}
