package com.transport.subscription.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO pour activer/désactiver le renouvellement automatique
 */
public class UpdateAutoRenewRequest {

    @NotNull(message = "Subscription ID is required")
    private UUID subscriptionId;

    @NotNull(message = "Auto-renew status is required")
    private Boolean autoRenew;

    private String paymentMethodId;

    // Getters and Setters
    public UUID getSubscriptionId() { return subscriptionId; }
    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}