package com.transport.subscription.model;

public enum SubscriptionStatus {
    PENDING,     // En attente de paiement
    ACTIVE,      // Actif et valide
    EXPIRED,     // Expiré
    CANCELLED,   // Annulé par l'utilisateur
    SUSPENDED    // Suspendu (ex: paiement échoué)
}