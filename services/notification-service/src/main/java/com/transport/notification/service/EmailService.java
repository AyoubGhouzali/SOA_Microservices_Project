package com.transport.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * Service d'envoi d'emails
 *
 * Utilise Spring Boot Mail avec SMTP
 * Supporte les emails simples (text) et HTML
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@transport.com}")
    private String fromEmail;

    @Value("${notification.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * Envoie un email simple (texte)
     *
     * @param to Destinataire
     * @param subject Sujet
     * @param text Contenu
     */
    public void sendEmail(String to, String subject, String text) {
        if (!emailEnabled) {
            log.warn("Email notifications are disabled. Skipping email to: {}", to);
            return;
        }

        try {
            log.info("📧 Sending email to: {} | Subject: {}", to, subject);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);

            log.info("✅ Email sent successfully to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    /**
     * Envoie un email HTML (avec mise en forme)
     *
     * @param to Destinataire
     * @param subject Sujet
     * @param htmlContent Contenu HTML
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (!emailEnabled) {
            log.warn("Email notifications are disabled. Skipping HTML email to: {}", to);
            return;
        }

        try {
            log.info("📧 Sending HTML email to: {} | Subject: {}", to, subject);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(message);

            log.info("✅ HTML email sent successfully to: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Failed to send HTML email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send HTML email: " + e.getMessage(), e);
        }
    }

    /**
     * Construit un email HTML formaté pour une notification de bus
     */
    public String buildBusNotificationHtml(
            String busNumber,
            String routeNumber,
            String eventType,
            String message,
            String timestamp
    ) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }
                    .container { background-color: white; padding: 30px; border-radius: 10px; max-width: 600px; margin: 0 auto; }
                    .header { background-color: #2196F3; color: white; padding: 20px; border-radius: 5px; text-align: center; }
                    .content { padding: 20px; line-height: 1.6; }
                    .info-box { background-color: #e3f2fd; padding: 15px; border-left: 4px solid #2196F3; margin: 15px 0; }
                    .footer { text-align: center; color: #666; font-size: 12px; margin-top: 20px; }
                    .urgent { background-color: #ffebee; border-left-color: #f44336; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h2>🚌 Notification Transport</h2>
                    </div>
                    <div class="content">
                        <h3>%s</h3>
                        <div class="info-box">
                            <p><strong>Bus:</strong> %s</p>
                            <p><strong>Ligne:</strong> %s</p>
                            <p><strong>Heure:</strong> %s</p>
                        </div>
                        <p>%s</p>
                    </div>
                    <div class="footer">
                        <p>Système de Transport - Notifications automatiques</p>
                        <p>Cet email a été généré automatiquement, merci de ne pas y répondre.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(eventType, busNumber, routeNumber, timestamp, message);
    }
}
