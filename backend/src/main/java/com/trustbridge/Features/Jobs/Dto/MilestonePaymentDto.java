package com.trustbridge.Features.Jobs.Dto;

import java.math.BigDecimal;

public record MilestonePaymentDto(
        String jobTitle,
        String freelancerName,
        String clientName,
        String clientEmail,
        String currency,
        String milestoneTitle,
        String milestoneAmount,
        String clientSecret,
        String PaymentRequestId,
        String provider
) {
}
