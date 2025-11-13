package com.transport.ticketing.domain.service;

import com.transport.ticketing.domain.model.TicketType;
import java.math.BigDecimal;

/**
 * Domain Service for pricing logic
 * Use domain services when logic doesn't belong to a single entity
 */
public class PricingService {
    
    // Base prices (in a real system, these might come from database)
    private static final BigDecimal SINGLE_PRICE = new BigDecimal("2.50");
    private static final BigDecimal DAILY_PRICE = new BigDecimal("10.00");
    private static final BigDecimal WEEKLY_PRICE = new BigDecimal("35.00");
    private static final BigDecimal MONTHLY_PRICE = new BigDecimal("120.00");
    
    /**
     * Calculate price for ticket purchase
     * @param type The ticket type
     * @param quantity Number of tickets
     * @return Total price
     */
    public BigDecimal calculatePrice(TicketType type, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        BigDecimal basePrice = getBasePrice(type);
        BigDecimal total = basePrice.multiply(BigDecimal.valueOf(quantity));
        
        // Apply bulk discount (example business rule)
        if (quantity >= 10) {
            total = applyDiscount(total, 10); // 10% discount
        } else if (quantity >= 5) {
            total = applyDiscount(total, 5);  // 5% discount
        }
        
        return total;
    }
    
    /**
     * Calculate price for a single ticket type
     */
    public BigDecimal calculateSinglePrice(TicketType type) {
        return getBasePrice(type);
    }
    
    private BigDecimal getBasePrice(TicketType type) {
        return switch (type) {
            case SINGLE -> SINGLE_PRICE;
            case DAILY -> DAILY_PRICE;
            case WEEKLY -> WEEKLY_PRICE;
            case MONTHLY -> MONTHLY_PRICE;
        };
    }
    
    private BigDecimal applyDiscount(BigDecimal amount, int discountPercent) {
        BigDecimal discount = amount.multiply(BigDecimal.valueOf(discountPercent))
                                    .divide(BigDecimal.valueOf(100));
        return amount.subtract(discount);
    }
}