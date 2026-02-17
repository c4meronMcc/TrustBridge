package com.trustbridge.Domain.Enums;

public class MilestoneStatus {

    public enum milestoneStatus{
        LOCKED,
        AWAITING_PAYMENT,
        IN_PROGRESS,
        SUBMITTED,
        APPROVED,
        PAID_OUT,
        DISPUTE_NEGOTIATION,
        DISPUTE_ARBITRATION,
        DISPUTE_RESOLVED,
        CANCELLED
    }

}
