package com.trustbridge.Features.Jobs.Dto;

public record PaymentActivationDto(
        String clientSecret,
        String paymentRequestId,
        String provider
) {}
