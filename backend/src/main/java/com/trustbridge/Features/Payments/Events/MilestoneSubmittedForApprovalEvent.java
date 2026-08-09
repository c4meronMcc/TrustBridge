package com.trustbridge.Features.Payments.Events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class MilestoneSubmittedForApprovalEvent extends ApplicationEvent {
    private final UUID milestoneId;

    public MilestoneSubmittedForApprovalEvent(Object source, UUID milestoneId) {
        super(source);
        this.milestoneId = milestoneId;
    }

    public UUID getMilestoneId() {
        return milestoneId;
    }
}
