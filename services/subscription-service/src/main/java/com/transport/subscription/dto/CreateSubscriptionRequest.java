package com.transport.subscription.dto;

import com.transport.subscription.model.SubscriptionType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * DTO pour créer un abonnement
 */
public class CreateSubscriptionRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Subscription type is required")
    private SubscriptionType type;

    private Boolean autoRenew = false;

    private String paymentMethodId;

    // Getters and Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public SubscriptionType getType() { return type; }
    public void setType(SubscriptionType type) { this.type = type; }

    public Boolean getAutoRenew() { return autoRenew; }
    public void setAutoRenew(Boolean autoRenew) { this.autoRenew = autoRenew; }

    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}