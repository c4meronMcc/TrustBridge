package com.trustbridge.Features.Jobs.Events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class MilestoneApprovedEvent extends ApplicationEvent {
    private final UUID milestoneId;

    public MilestoneApprovedEvent(Object source, UUID milestoneId) {
        super(source);
        this.milestoneId = milestoneId;
    }

    public UUID getMilestoneId() {
        return milestoneId;
    }
}
