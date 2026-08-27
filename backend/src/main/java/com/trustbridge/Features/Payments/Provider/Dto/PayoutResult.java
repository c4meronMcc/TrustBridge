package com.trustbridge.Features.Payments.Provider.Dto;

public record PayoutResult(
        String payoutReferenceId,
        long amountPaidOutMinorUnits
) { }
