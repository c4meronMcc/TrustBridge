package com.trustbridge.Features.Jobs.Events;


import com.trustbridge.Domain.Enums.JobEvent;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class UnlockNextMilestoneEvent extends ApplicationEvent {
    private final UUID jobId;

    public UnlockNextMilestoneEvent(Object source, UUID jobId) {
        super(source);
        this.jobId = jobId;
    }

    public UUID getJobId() {
        return jobId;
    }

}
