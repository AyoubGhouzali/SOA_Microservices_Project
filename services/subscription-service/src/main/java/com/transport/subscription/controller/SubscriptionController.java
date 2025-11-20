package com.transport.subscription.controller;

import com.transport.subscription.dto.CreateSubscriptionRequest;
import com.transport.subscription.dto.SubscriptionResponse;
import com.transport.subscription.dto.UpdateAutoRenewRequest;
import com.transport.subscription.model.SubscriptionStatus;
import com.transport.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST Controller pour la gestion des abonnements
 *
 * Base URL: /api/subscriptions
 */
@RestController
@RequestMapping("/api/subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /**
     * Health Check
     * GET /api/subscriptions/health
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Subscription Service is UP");
    }

    /**
     * Créer un nouvel abonnement
     * POST /api/subscriptions
     *
     * Body:
     * {
     *   "userId": "uuid",
     *   "type": "MONTHLY",
     *   "autoRenew": false,
     *   "paymentMethodId": "pm_123456"
     * }
     */
    @PostMapping
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        logger.info("POST /api/subscriptions - Creating subscription for user: {}",
                request.getUserId());

        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Activer un abonnement (après paiement)
     * PUT /api/subscriptions/{id}/activate
     */
    @PutMapping("/{id}/activate")
    public ResponseEntity<SubscriptionResponse> activateSubscription(@PathVariable UUID id) {
        logger.info("PUT /api/subscriptions/{}/activate", id);

        SubscriptionResponse response = subscriptionService.activateSubscription(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Annuler un abonnement
     * PUT /api/subscriptions/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable UUID id) {
        logger.info("PUT /api/subscriptions/{}/cancel", id);

        SubscriptionResponse response = subscriptionService.cancelSubscription(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Renouveler un abonnement manuellement
     * PUT /api/subscriptions/{id}/renew
     */
    @PutMapping("/{id}/renew")
    public ResponseEntity<SubscriptionResponse> renewSubscription(@PathVariable UUID id) {
        logger.info("PUT /api/subscriptions/{}/renew", id);

        SubscriptionResponse response = subscriptionService.renewSubscription(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Suspendre un abonnement
     * PUT /api/subscriptions/{id}/suspend
     */
    @PutMapping("/{id}/suspend")
    public ResponseEntity<SubscriptionResponse> suspendSubscription(@PathVariable UUID id) {
        logger.info("PUT /api/subscriptions/{}/suspend", id);

        SubscriptionResponse response = subscriptionService.suspendSubscription(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mettre à jour le renouvellement automatique
     * PUT /api/subscriptions/auto-renew
     *
     * Body:
     * {
     *   "subscriptionId": "uuid",
     *   "autoRenew": true,
     *   "paymentMethodId": "pm_123456"
     * }
     */
    @PutMapping("/auto-renew")
    public ResponseEntity<SubscriptionResponse> updateAutoRenew(
            @Valid @RequestBody UpdateAutoRenewRequest request) {
        logger.info("PUT /api/subscriptions/auto-renew - Subscription: {}",
                request.getSubscriptionId());

        SubscriptionResponse response = subscriptionService.updateAutoRenew(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer un abonnement par ID
     * GET /api/subscriptions/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable UUID id) {
        logger.info("GET /api/subscriptions/{}", id);

        SubscriptionResponse subscription = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Récupérer tous les abonnements d'un utilisateur
     * GET /api/subscriptions/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(
            @PathVariable UUID userId) {
        logger.info("GET /api/subscriptions/user/{}", userId);

        List<SubscriptionResponse> subscriptions =
                subscriptionService.getUserSubscriptions(userId);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Récupérer l'abonnement actif d'un utilisateur
     * GET /api/subscriptions/user/{userId}/active
     */
    @GetMapping("/user/{userId}/active")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription(
            @PathVariable UUID userId) {
        logger.info("GET /api/subscriptions/user/{}/active", userId);

        SubscriptionResponse subscription =
                subscriptionService.getActiveSubscription(userId);
        return ResponseEntity.ok(subscription);
    }

    /**
     * Vérifier si un utilisateur a un abonnement actif
     * GET /api/subscriptions/user/{userId}/has-active
     */
    @GetMapping("/user/{userId}/has-active")
    public ResponseEntity<Boolean> hasActiveSubscription(@PathVariable UUID userId) {
        logger.info("GET /api/subscriptions/user/{}/has-active", userId);

        boolean hasActive = subscriptionService.hasActiveSubscription(userId);
        return ResponseEntity.ok(hasActive);
    }

    /**
     * Récupérer les abonnements par statut
     * GET /api/subscriptions/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptionsByStatus(
            @PathVariable SubscriptionStatus status) {
        logger.info("GET /api/subscriptions/status/{}", status);

        List<SubscriptionResponse> subscriptions =
                subscriptionService.getSubscriptionsByStatus(status);
        return ResponseEntity.ok(subscriptions);
    }

    /**
     * Supprimer un abonnement
     * DELETE /api/subscriptions/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(@PathVariable UUID id) {
        logger.info("DELETE /api/subscriptions/{}", id);

        subscriptionService.deleteSubscription(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtenir des statistiques
     * GET /api/subscriptions/statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<SubscriptionService.SubscriptionStatistics> getStatistics() {
        logger.info("GET /api/subscriptions/statistics");

        SubscriptionService.SubscriptionStatistics stats =
                subscriptionService.getStatistics();
        return ResponseEntity.ok(stats);
    }
}