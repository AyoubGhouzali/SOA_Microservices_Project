package com.transport.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * DTO pour les requêtes manuelles de notification via API REST
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Le destinataire est requis")
    private String recipient;

    @Email(message = "Email invalide")
    private String email;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Numéro de téléphone invalide")
    private String phoneNumber;

    @NotBlank(message = "Le sujet est requis")
    private String subject;

    @NotBlank(message = "Le message est requis")
    private String message;

    // Type de notification : EMAIL, SMS, BOTH
    private String notificationType;

    // Priorité
    private String priority;
}
