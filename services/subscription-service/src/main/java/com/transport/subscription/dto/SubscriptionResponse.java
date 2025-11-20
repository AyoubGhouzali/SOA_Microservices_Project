package com.transport.subscription.dto;

import com.transport.subscription.model.Subscription;
import com.transport.subscription.model.SubscriptionStatus;
import com.transport.subscription.model.SubscriptionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de réponse pour Subscription
 */
public class SubscriptionResponse {

    private UUID id;
    private UUID userId;
    private SubscriptionType type;
    private SubscriptionStatus status;
    private BigDecimal price;
    private String currency;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long daysRemaining;
    private Boolean autoRenew;
    private String paymentMethodId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastRenewalDate;
    private Boolean isValid;

    // Factory method
    public static SubscriptionResponse fromEntity(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setUserId(subscription.getUserId());
        response.setType(subscription.getType());
        response.setStatus(subscription.getStatus());
        response.setPrice(subscription.getPrice());
        response.setCurrency(subscription.getCurrency());
        response.setStartDate(subscription.getStartDate());
        response.setEndDate(subscription.getEndDate());
        response.setDaysRemaining(subscription.getDaysRemaining());
        response.setAutoRenew(subscription.getAutoRenew());
        response.setPaymentMethodId(subscription.getPaymentMethodId());
        response.setCreatedAt(subscription.getCreatedAt());
        response.setUpdatedAt(subscription.getUpdatedAt());
        response.setLastRenewalDate(subscription.getLastRenewalDate());
        response.setIsValid(subscription.isValid());
        return response;
    }

    // Getters and Setters
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

    public Long getDaysRemaining() { return daysRemaining; }
    public void setDaysRemaining(Long daysRemaining) { this.daysRemaining = daysRemaining; }

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

    public Boolean getIsValid() { return isValid; }
    public void setIsValid(Boolean isValid) { this.isValid = isValid; }
}