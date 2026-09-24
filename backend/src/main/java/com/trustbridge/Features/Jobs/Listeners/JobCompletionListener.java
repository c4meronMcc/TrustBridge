package com.trustbridge.Features.Jobs.Listeners;

import com.trustbridge.Domain.Enums.JobEvent.jobEvent;
import com.trustbridge.Features.Jobs.Events.AllMilestonesCompletedEvent;
import com.trustbridge.Features.Jobs.Service.JobStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobCompletionListener {

    private final JobStateService jobStateService;

    /**
     * Fired once the last milestone in a job's sequence has been client-approved and
     * released. Since each milestone already pays the freelancer out of escrow
     * individually, there's no separate job-level payment left to release once the last
     * milestone is done — so the client's one approval on that final milestone doubles
     * as the job's final approval. Rather than making them approve twice, we walk the
     * job's own state machine the rest of the way through in one go:
     *   IN_PROGRESS -> SUBMITTED -> APPROVED -> PAID_OUT
     */
    @EventListener
    public void onAllMilestonesCompleted(AllMilestonesCompletedEvent event) {
        UUID jobId = event.getJobId();

        jobStateService.allMilestonesCompleted(jobId);
        jobStateService.fireEvent(jobId, jobEvent.JOB_APPROVED, Map.of("isJobApproved", true));
        jobStateService.fireEvent(jobId, jobEvent.RELEASE_PAYMENT, Map.of("isJobReleased", true));
    }
}