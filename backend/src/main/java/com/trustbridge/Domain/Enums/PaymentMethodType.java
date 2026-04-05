package com.trustbridge.Domain.Enums;

import java.math.BigDecimal;

public enum PaymentMethodType {

    CARD(BigDecimal.ZERO, new BigDecimal("400")),
    VIBAN(new BigDecimal("400"), new BigDecimal("20000")),
    ESCROW(new BigDecimal("20000"), null);

    private final BigDecimal minAmount;
    private final BigDecimal maxAmount;

    PaymentMethodType(BigDecimal minAmount, BigDecimal maxAmount) {
        this.minAmount = minAmount;
        this.maxAmount = maxAmount;
    }

    public static PaymentMethodType fromAmount(BigDecimal amount) {
        for (PaymentMethodType type : values()) {
            if (type.maxAmount == null || amount.compareTo(type.maxAmount) < 0) {
                if (amount.compareTo(type.minAmount) >= 0) {
                    return type;
                }
            }
        }
        throw new IllegalArgumentException("Could not determine payment method type for amount: " + amount);
    }
}
