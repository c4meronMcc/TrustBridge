package com.trustbridge.Features.Dashboard.Freelancer.Dto;

import java.math.BigDecimal;

public record FinancialMetricsDto(
        BigDecimal fundsInEscrowHolding,
        BigDecimal fundsPending,
        BigDecimal fundsPaidOut
) {
    // 1. The primary constructor for when Hibernate passes strict BigDecimals
    public FinancialMetricsDto(BigDecimal fundsInEscrowHolding, BigDecimal fundsPending, BigDecimal fundsPaidOut) {
        this.fundsInEscrowHolding = fundsInEscrowHolding != null ? fundsInEscrowHolding : BigDecimal.ZERO;
        this.fundsPending = fundsPending != null ? fundsPending : BigDecimal.ZERO;
        this.fundsPaidOut = fundsPaidOut != null ? fundsPaidOut : BigDecimal.ZERO;
    }

    // 2. The Universal Fallback Constructor
    // Catches Integer (int), Long, Double, or Float and safely converts them to BigDecimal
    public FinancialMetricsDto(Number fundsInEscrowHolding, Number fundsPending, Number fundsPaidOut) {
        this(
                fundsInEscrowHolding != null ? new BigDecimal(fundsInEscrowHolding.toString()) : BigDecimal.ZERO,
                fundsPending != null ? new BigDecimal(fundsPending.toString()) : BigDecimal.ZERO,
                fundsPaidOut != null ? new BigDecimal(fundsPaidOut.toString()) : BigDecimal.ZERO
        );
    }
}