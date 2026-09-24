package com.trustbridge.Features.Jobs.Events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Fired when the final milestone in a job's sequence has been paid out, so the job's
 * own state machine can be advanced (IN_PROGRESS -> SUBMITTED, via ALL_MILESTONES_COMPLETED)
 * rather than unlocking a next milestone that doesn't exist.
 */
public class AllMilestonesCompletedEvent extends ApplicationEvent {
    private final UUID jobId;

    public AllMilestonesCompletedEvent(Object source, UUID jobId) {
        super(source);
        this.jobId = jobId;
    }

    public UUID getJobId() {
        return jobId;
    }
}