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

        Milestones milestone = milestoneRepository.findById(event.getMilestoneId())
                .orElseThrow(() -> new RuntimeException("Milestone not found for funding event!"));


        milestone.setStatus(MilestoneStatus.milestoneStatus.IN_PROGRESS);
        milestoneRepository.save(milestone);

        log.info("✅ Milestone {} successfully updated to IN_PROGRESS", milestone.getId());

        Jobs job = milestone.getJob();

        if ("AWAITING_PAYMENT".equals(job.getStatus().name()) || "PENDING_ACCEPTANCE".equals(job.getStatus().name())) {

            job.setStatus(JobStatus.jobStatus.IN_PROGRESS);
            jobRepository.save(job);

            log.info("✅ Parent Job {} successfully updated to IN_PROGRESS", job.getId());
        }
    }
}
