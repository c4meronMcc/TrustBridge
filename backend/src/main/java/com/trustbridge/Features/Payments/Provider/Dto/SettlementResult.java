package com.trustbridge.Features.Payments.Provider.Dto;

public record SettlementResult(
        String token,
        String providerReferenceId,
        String redirectUrl
) {}
