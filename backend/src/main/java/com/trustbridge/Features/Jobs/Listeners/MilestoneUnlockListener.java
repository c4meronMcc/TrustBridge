package com.trustbridge.Features.Jobs.Listeners;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Events.MilestoneApprovedEvent;
import com.trustbridge.Features.Jobs.Events.UnlockNextMilestoneEvent;
import com.trustbridge.Features.Jobs.Service.MilestoneStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MilestoneUnlockListener {

    private final MilestoneStateService milestoneStateService;
    private final MilestoneRepository milestoneRepository;

    @EventListener
    public void onUnlockNextMilestone(UnlockNextMilestoneEvent event) {
        milestoneStateService.activateNextLockedMilestoneForJob(event.getJobId());
    }
}
