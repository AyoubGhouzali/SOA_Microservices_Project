package com.transport.notification.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

/**
 * Service d'envoi de SMS via Twilio
 *
 * Twilio est un service cloud pour envoyer des SMS/appels
 * Documentation: https://www.twilio.com/docs/sms
 *
 * IMPORTANT: Pour utiliser ce service en production :
 * 1. Créer un compte Twilio (gratuit pour test)
 * 2. Obtenir le ACCOUNT_SID et AUTH_TOKEN
 * 3. Obtenir un numéro de téléphone Twilio
 * 4. Configurer ces valeurs dans application.yml
 */
@Slf4j
@Service
public class SmsService {

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromPhoneNumber;

    @Value("${notification.sms.enabled:false}")
    private boolean smsEnabled;

    /**
     * Initialise Twilio avec les credentials
     * Appelé automatiquement au démarrage du service
     */
    @PostConstruct
    public void init() {
        if (smsEnabled && accountSid != null && !accountSid.isEmpty() &&
            authToken != null && !authToken.isEmpty()) {
            try {
                Twilio.init(accountSid, authToken);
                log.info("✅ Twilio SMS service initialized successfully");
            } catch (Exception e) {
                log.error("❌ Failed to initialize Twilio: {}", e.getMessage());
                smsEnabled = false;
            }
        } else {
            log.warn("⚠️  SMS notifications disabled - Twilio credentials not configured");
            log.info("ℹ️  To enable SMS: configure twilio.account-sid, twilio.auth-token, and twilio.phone-number");
        }
    }

    /**
     * Envoie un SMS
     *
     * @param to Numéro de téléphone du destinataire (format: +33612345678)
     * @param messageText Texte du SMS (max 160 caractères recommandé)
     */
    public void sendSms(String to, String messageText) {
        if (!smsEnabled) {
            log.warn("📱 SMS notifications are disabled. Would send to {}: {}", to, messageText);
            // En mode développement, on simule l'envoi
            simulateSmsDelivery(to, messageText);
            return;
        }

        try {
            log.info("📱 Sending SMS to: {} | Message: {}", to, messageText);

            // Twilio API call
            Message message = Message.creator(
                    new PhoneNumber(to),              // To
                    new PhoneNumber(fromPhoneNumber), // From (your Twilio number)
                    messageText                       // Message body
            ).create();

            log.info("✅ SMS sent successfully to: {} | SID: {}", to, message.getSid());
            log.info("   Status: {} | Price: {}", message.getStatus(), message.getPrice());

        } catch (Exception e) {
            log.error("❌ Failed to send SMS to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send SMS: " + e.getMessage(), e);
        }
    }

    /**
     * Simule l'envoi d'un SMS (pour développement/test)
     * Utilisé quand Twilio n'est pas configuré
     */
    private void simulateSmsDelivery(String to, String messageText) {
        log.info("=".repeat(60));
        log.info("📱 SIMULATED SMS DELIVERY");
        log.info("To: {}", to);
        log.info("From: {}", fromPhoneNumber.isEmpty() ? "+1234567890" : fromPhoneNumber);
        log.info("Message:");
        log.info("-".repeat(60));
        log.info("{}", messageText);
        log.info("=".repeat(60));
    }

    /**
     * Envoie un SMS avec formatage pour notifications de transport
     */
    public void sendBusNotificationSms(
            String to,
            String busNumber,
            String routeNumber,
            String eventType,
            String shortMessage
    ) {
        String formattedMessage = String.format(
            "🚌 TRANSPORT ALERT\n" +
            "Bus %s - Ligne %s\n" +
            "%s\n" +
            "%s",
            busNumber,
            routeNumber,
            eventType,
            shortMessage
        );

        // Limiter à 160 caractères
        if (formattedMessage.length() > 160) {
            formattedMessage = formattedMessage.substring(0, 157) + "...";
        }

        sendSms(to, formattedMessage);
    }
}
