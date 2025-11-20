package com.transport.subscription.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entité Subscription - Représente un abonnement
 * Stocké dans PostgreSQL
 */
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscription_user_id", columnList = "user_id"),
        @Index(name = "idx_subscription_status", columnList = "status"),
        @Index(name = "idx_subscription_end_date", columnList = "end_date"),
        @Index(name = "idx_subscription_auto_renew", columnList = "auto_renew")
})
public class Subscription {

    @Id
    @Column(name = "subscription_id")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SubscriptionType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "auto_renew", nullable = false)
    private Boolean autoRenew;

    @Column(name = "payment_method_id", length = 100)
    private String paymentMethodId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_renewal_date")
    private LocalDateTime lastRenewalDate;

    // Constructor
    public Subscription() {
        this.id = UUID.randomUUID();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = SubscriptionStatus.PENDING;
        this.currency = "USD";
        this.autoRenew = false;
    }

    // ============================================
    // BUSINESS METHODS
    // ============================================

    /**
     * Activer l'abonnement après paiement réussi
     */
    public void activate() {
        if (this.status == SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Subscription is already active");
        }

        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot activate a cancelled subscription");
        }

        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDate.now();
        this.endDate = calculateEndDate(this.startDate, this.type);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Renouveler l'abonnement
     */
    public void renew() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot renew a cancelled subscription");
        }

        if (!this.autoRenew) {
            throw new IllegalStateException("Auto-renewal is not enabled");
        }

        this.startDate = this.endDate.plusDays(1);
        this.endDate = calculateEndDate(this.startDate, this.type);
        this.status = SubscriptionStatus.ACTIVE;
        this.lastRenewalDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Annuler l'abonnement
     */
    public void cancel() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Subscription is already cancelled");
        }

        this.status = SubscriptionStatus.CANCELLED;
        this.autoRenew = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Suspendre l'abonnement
     */
    public void suspend() {
        if (this.status == SubscriptionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot suspend a cancelled subscription");
        }

        this.status = SubscriptionStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Vérifier si l'abonnement est valide aujourd'hui
     */
    public boolean isValid() {
        if (this.status != SubscriptionStatus.ACTIVE) {
            return false;
        }

        LocalDate today = LocalDate.now();
        return !today.isBefore(this.startDate) && !today.isAfter(this.endDate);
    }

    /**
     * Vérifier si l'abonnement doit être renouvelé
     */
    public boolean shouldRenew() {
        if (!this.autoRenew || this.status != SubscriptionStatus.ACTIVE) {
            return false;
        }

        // Renouveler 3 jours avant l'expiration
        LocalDate renewalDate = this.endDate.minusDays(3);
        return LocalDate.now().isAfter(renewalDate) || LocalDate.now().isEqual(renewalDate);
    }

    /**
     * Marquer comme expiré
     */
    public void markAsExpired() {
        if (this.status == SubscriptionStatus.ACTIVE && LocalDate.now().isAfter(this.endDate)) {
            this.status = SubscriptionStatus.EXPIRED;
            this.updatedAt = LocalDateTime.now();
        }
    }

    /**
     * Activer le renouvellement automatique
     */
    public void enableAutoRenew(String paymentMethodId) {
        if (paymentMethodId == null || paymentMethodId.isBlank()) {
            throw new IllegalArgumentException("Payment method is required for auto-renewal");
        }

        this.autoRenew = true;
        this.paymentMethodId = paymentMethodId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Désactiver le renouvellement automatique
     */
    public void disableAutoRenew() {
        this.autoRenew = false;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculer la date de fin selon le type
     */
    private LocalDate calculateEndDate(LocalDate startDate, SubscriptionType type) {
        return switch (type) {
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case QUARTERLY -> startDate.plusMonths(3).minusDays(1);
            case ANNUAL -> startDate.plusYears(1).minusDays(1);
        };
    }

    /**
     * Calculer le nombre de jours restants
     */
    public long getDaysRemaining() {
        if (!isValid()) {
            return 0;
        }

        LocalDate today = LocalDate.now();
        return java.time.temporal.ChronoUnit.DAYS.between(today, this.endDate);
    }

    // ============================================
    // GETTERS AND SETTERS
    // ============================================

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public SubscriptionType getType() { return type; }
    public void setType(SubscriptionType type) { this.type = type; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastRenewalDate() { return lastRenewalDate; }
    public void setLastRenewalDate(LocalDateTime lastRenewalDate) {
        this.lastRenewalDate = lastRenewalDate;
    }
}