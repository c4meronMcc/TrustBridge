package com.trustbridge.Features.Jobs.Milestones;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Features.Jobs.Dto.MilestoneDto;
import com.trustbridge.Features.Jobs.JobService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class MilestoneService {

    JobService jobService;

    public MilestoneService(JobService jobService) {
        this.jobService = jobService;
    }

    @Transactional
    public void createMilestonesForJob(Jobs jobs) {

    }

}
