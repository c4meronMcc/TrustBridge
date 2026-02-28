package com.trustbridge.Domain.Enums;

public class JobStatus {
    public enum jobStatus{
        DRAFT,
        PENDING_ACCEPTANCE,
        AWAITING_PAYMENT,
        IN_PROGRESS,
        SUBMITTED,
        APPROVED,
        PAID_OUT,
        DISPUTED,
        CANCELLED
    }
}
