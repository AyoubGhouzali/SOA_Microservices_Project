package com.transport.subscription.service;

import com.transport.subscription.dto.CreateSubscriptionRequest;
import com.transport.subscription.dto.SubscriptionResponse;
import com.transport.subscription.dto.UpdateAutoRenewRequest;
import com.transport.subscription.exception.ResourceAlreadyExistsException;
import com.transport.subscription.exception.ResourceNotFoundException;
import com.transport.subscription.model.Subscription;
import com.transport.subscription.model.SubscriptionStatus;
import com.transport.subscription.model.SubscriptionType;
import com.transport.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des abonnements
 */
@Service
@Transactional
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    // Prix des abonnements
    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("50.00");
    private static final BigDecimal QUARTERLY_PRICE = new BigDecimal("135.00");  // 10% réduction
    private static final BigDecimal ANNUAL_PRICE = new BigDecimal("480.00");     // 20% réduction

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Créer un nouvel abonnement
     */
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        logger.info("Creating subscription for user: {}, type: {}",
                request.getUserId(), request.getType());

        // Vérifier si l'utilisateur a déjà un abonnement actif
        if (subscriptionRepository.existsActiveSubscriptionForUser(request.getUserId())) {
            throw new ResourceAlreadyExistsException(
                    "User already has an active subscription");
        }

        // Créer l'abonnement
        Subscription subscription = new Subscription();
        subscription.setUserId(request.getUserId());
        subscription.setType(request.getType());
        subscription.setPrice(calculatePrice(request.getType()));
        subscription.setAutoRenew(request.getAutoRenew() != null ? request.getAutoRenew() : false);

        if (request.getAutoRenew() != null && request.getAutoRenew()) {
            if (request.getPaymentMethodId() == null || request.getPaymentMethodId().isBlank()) {
                throw new IllegalArgumentException(
                        "Payment method is required for auto-renewal");
            }
            subscription.setPaymentMethodId(request.getPaymentMethodId());
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logger.info("Subscription created successfully: {}", savedSubscription.getId());

        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Activer un abonnement (après paiement réussi)
     */
    public SubscriptionResponse activateSubscription(UUID subscriptionId) {
        logger.info("Activating subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + subscriptionId));

        subscription.activate();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        logger.info("Subscription activated successfully: {}", subscriptionId);
        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Annuler un abonnement
     */
    public SubscriptionResponse cancelSubscription(UUID subscriptionId) {
        logger.info("Cancelling subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + subscriptionId));

        subscription.cancel();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        logger.info("Subscription cancelled successfully: {}", subscriptionId);
        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Renouveler un abonnement
     */
    public SubscriptionResponse renewSubscription(UUID subscriptionId) {
        logger.info("Renewing subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + subscriptionId));

        subscription.renew();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        logger.info("Subscription renewed successfully: {}", subscriptionId);
        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Suspendre un abonnement
     */
    public SubscriptionResponse suspendSubscription(UUID subscriptionId) {
        logger.info("Suspending subscription: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + subscriptionId));

        subscription.suspend();
        Subscription savedSubscription = subscriptionRepository.save(subscription);

        logger.info("Subscription suspended successfully: {}", subscriptionId);
        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Mettre à jour le renouvellement automatique
     */
    public SubscriptionResponse updateAutoRenew(UpdateAutoRenewRequest request) {
        logger.info("Updating auto-renew for subscription: {}",
                request.getSubscriptionId());

        Subscription subscription = subscriptionRepository.findById(request.getSubscriptionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + request.getSubscriptionId()));

        if (request.getAutoRenew()) {
            subscription.enableAutoRenew(request.getPaymentMethodId());
        } else {
            subscription.disableAutoRenew();
        }

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        logger.info("Auto-renew updated successfully for subscription: {}",
                request.getSubscriptionId());

        return SubscriptionResponse.fromEntity(savedSubscription);
    }

    /**
     * Récupérer un abonnement par ID
     */
    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        logger.info("Fetching subscription: {}", id);

        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subscription not found: " + id));

        return SubscriptionResponse.fromEntity(subscription);
    }

    /**
     * Récupérer tous les abonnements d'un utilisateur
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getUserSubscriptions(UUID userId) {
        logger.info("Fetching subscriptions for user: {}", userId);

        return subscriptionRepository.findByUserId(userId).stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer l'abonnement actif d'un utilisateur
     */
    @Transactional(readOnly = true)
    public SubscriptionResponse getActiveSubscription(UUID userId) {
        logger.info("Fetching active subscription for user: {}", userId);

        Subscription subscription = subscriptionRepository
                .findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE)
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new ResourceNotFoundException("No active subscription found for user: " + userId));

        return SubscriptionResponse.fromEntity(subscription);
    }


    /**
     * Vérifier si un utilisateur a un abonnement actif
     */
    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID userId) {
        return subscriptionRepository.existsActiveSubscriptionForUser(userId);
    }

    /**
     * Récupérer les abonnements par statut
     */
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByStatus(SubscriptionStatus status) {
        logger.info("Fetching subscriptions with status: {}", status);

        return subscriptionRepository.findByUserIdAndStatus(null, status).stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Traiter les abonnements qui expirent
     * (À exécuter par un scheduler quotidiennement)
     */
    public void processExpiringSubscriptions() {
        logger.info("Processing expiring subscriptions");

        LocalDate today = LocalDate.now();
        List<Subscription> expired = subscriptionRepository.findExpired(today);

        for (Subscription subscription : expired) {
            subscription.markAsExpired();
            subscriptionRepository.save(subscription);
            logger.info("Subscription {} marked as expired", subscription.getId());
        }

        logger.info("Processed {} expired subscriptions", expired.size());
    }

    /**
     * Traiter les renouvellements automatiques
     * (À exécuter par un scheduler quotidiennement)
     */
    public void processAutoRenewals() {
        logger.info("Processing auto-renewals");

        LocalDate renewalDate = LocalDate.now().plusDays(3);
        List<Subscription> toRenew = subscriptionRepository.findToRenew(renewalDate);

        for (Subscription subscription : toRenew) {
            try {
                subscription.renew();
                subscriptionRepository.save(subscription);
                logger.info("Subscription {} renewed successfully", subscription.getId());

                // TODO: Publier événement pour déclencher le paiement
                // publishSubscriptionRenewedEvent(subscription);

            } catch (Exception e) {
                logger.error("Failed to renew subscription {}: {}",
                        subscription.getId(), e.getMessage());
            }
        }

        logger.info("Processed {} auto-renewals", toRenew.size());
    }

    /**
     * Supprimer un abonnement
     */
    public void deleteSubscription(UUID subscriptionId) {
        logger.info("Deleting subscription: {}", subscriptionId);

        if (!subscriptionRepository.existsById(subscriptionId)) {
            throw new ResourceNotFoundException("Subscription not found: " + subscriptionId);
        }

        subscriptionRepository.deleteById(subscriptionId);
        logger.info("Subscription deleted: {}", subscriptionId);
    }

    /**
     * Obtenir des statistiques
     */
    @Transactional(readOnly = true)
    public SubscriptionStatistics getStatistics() {
        logger.info("Fetching subscription statistics");

        long total = subscriptionRepository.count();
        long active = subscriptionRepository.countByStatus(SubscriptionStatus.ACTIVE);
        long expired = subscriptionRepository.countByStatus(SubscriptionStatus.EXPIRED);
        long cancelled = subscriptionRepository.countByStatus(SubscriptionStatus.CANCELLED);

        return new SubscriptionStatistics(total, active, expired, cancelled);
    }

    /**
     * Calculer le prix selon le type d'abonnement
     */
    private BigDecimal calculatePrice(SubscriptionType type) {
        return switch (type) {
            case MONTHLY -> MONTHLY_PRICE;
            case QUARTERLY -> QUARTERLY_PRICE;
            case ANNUAL -> ANNUAL_PRICE;
        };
    }

    // Inner class pour les statistiques
    public static class SubscriptionStatistics {
        private long total;
        private long active;
        private long expired;
        private long cancelled;

        public SubscriptionStatistics(long total, long active, long expired, long cancelled) {
            this.total = total;
            this.active = active;
            this.expired = expired;
            this.cancelled = cancelled;
        }

        // Getters
        public long getTotal() { return total; }
        public long getActive() { return active; }
        public long getExpired() { return expired; }
        public long getCancelled() { return cancelled; }
    }
}