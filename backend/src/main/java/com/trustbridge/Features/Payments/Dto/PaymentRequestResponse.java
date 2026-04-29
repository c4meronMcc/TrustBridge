package com.trustbridge.Features.Payments.Dto;

import java.util.UUID;

public record PaymentRequestResponse(
        UUID paymentRequestId,
        String paymentType,      //
        String clientSecret,
        String redirectUrl// only present for card payments, null for VIBAN
) {

    public static PaymentRequestResponse stripe(UUID id, String clientSecret) {
        return new PaymentRequestResponse(id, "stripe", clientSecret, null);
    }

    public static PaymentRequestResponse escrow(UUID id, String redirectUrl) {
        return new PaymentRequestResponse(id, "escrow", null, redirectUrl);
    }
}
