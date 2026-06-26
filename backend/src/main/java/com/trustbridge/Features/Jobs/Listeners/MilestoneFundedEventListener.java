package com.trustbridge.Features.Jobs.Listeners;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Enums.JobStatus;
import com.trustbridge.Domain.Enums.MilestoneStatus; // Adjust if your enum is named differently
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MilestoneFundedEventListener {

    private final MilestoneRepository milestoneRepository;
    private final JobRepository jobRepository;

    @EventListener
    @Transactional
    public void handleMilestoneFunded(MilestoneFundedEvent event) {
        log.info("Catching MilestoneFundedEvent for Milestone ID: {}", event.getMilestoneId());

        // 1. Fetch the Milestone that was just paid for
        Milestones milestone = milestoneRepository.findById(event.getMilestoneId())
                .orElseThrow(() -> new RuntimeException("Milestone not found for funding event!"));

        // 2. Update the Milestone state to IN_PROGRESS
        // Note: Change 'IN_PROGRESS' to match your exact enum structure (e.g., MilestoneStatus.milestoneStatus.IN_PROGRESS)
        milestone.setStatus(MilestoneStatus.milestoneStatus.IN_PROGRESS);
        milestoneRepository.save(milestone);

        log.info("✅ Milestone {} successfully updated to IN_PROGRESS", milestone.getId());

        // 3. Cascade the state up to the parent Job
        Jobs job = milestone.getJob();

        // We only want to change the Job to IN_PROGRESS if it is currently waiting to start.
        // If this is Milestone 2, the job is already IN_PROGRESS, so we don't need to touch it.
        if ("AWAITING_PAYMENT".equals(job.getStatus().name()) || "PENDING_ACCEPTANCE".equals(job.getStatus().name())) {

            // Adjust according to your JobStatus enum
            job.setStatus(JobStatus.jobStatus.IN_PROGRESS);
            jobRepository.save(job);

            log.info("✅ Parent Job {} successfully updated to IN_PROGRESS", job.getId());
        }
    }
}
