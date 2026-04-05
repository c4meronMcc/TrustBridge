package com.trustbridge.Features.Payments.Dto;

import java.util.UUID;

public record PaymentRequestResponse(
        UUID paymentRequestId,
        String paymentType,      // "CARD" or "VIBAN"
        String clientSecret      // only present for card payments, null for VIBAN
) {
    public static PaymentRequestResponse card(UUID id, String clientSecret) {
        return new PaymentRequestResponse(id, "CARD", clientSecret);
    }

    public static PaymentRequestResponse viban(UUID id) {
        return new PaymentRequestResponse(id, "VIBAN", null);
    }
}
