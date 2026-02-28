package com.trustbridge.Domain.Enums;

public enum PaymentRequestEvent {
    INITIATE_PAYMENT,
    PAYMENT_SUCCESSFUL,
    PAYMENT_FAILED,
    RETRY_PAYMENT,
    EXPIRE_REQUEST,
    CANCEL_REQUEST,
    REFUND_REQUEST
}
