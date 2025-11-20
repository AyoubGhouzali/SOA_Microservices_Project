package com.transport.subscription.repository;

import com.transport.subscription.model.Subscription;
import com.transport.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour Subscription
 * Spring Data JPA génère automatiquement l'implémentation
 */
@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // Trouver tous les abonnements d'un utilisateur
    List<Subscription> findByUserId(UUID userId);

    // Trouver les abonnements d'un utilisateur par statut
    List<Subscription> findByUserIdAndStatus(UUID userId, SubscriptionStatus status);

    // Trouver l'abonnement actif d'un utilisateur

    // Vérifier si un utilisateur a un abonnement actif
    @Query("SELECT COUNT(s) > 0 FROM Subscription s WHERE s.userId = :userId AND s.status = 'ACTIVE'")
    boolean existsActiveSubscriptionForUser(@Param("userId") UUID userId);

    // Trouver les abonnements qui expirent bientôt
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate <= :expirationDate")
    List<Subscription> findExpiringSoon(@Param("expirationDate") LocalDate expirationDate);

    // Trouver les abonnements avec renouvellement automatique
    List<Subscription> findByAutoRenewTrue();

    // Trouver les abonnements à renouveler
    @Query("SELECT s FROM Subscription s WHERE s.autoRenew = true AND s.status = 'ACTIVE' AND s.endDate <= :renewalDate")
    List<Subscription> findToRenew(@Param("renewalDate") LocalDate renewalDate);

    // Trouver les abonnements expirés
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :today")
    List<Subscription> findExpired(@Param("today") LocalDate today);

    // Compter les abonnements par statut
    long countByStatus(SubscriptionStatus status);
}