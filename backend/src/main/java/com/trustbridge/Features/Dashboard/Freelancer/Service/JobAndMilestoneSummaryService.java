package com.trustbridge.Features.Dashboard.Freelancer.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.JobAndMilestoneData;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.JobSummaryDto;
import com.trustbridge.Features.Dashboard.Freelancer.Dto.MilestoneSummaryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JobAndMilestoneSummaryService {

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final MilestoneRepository milestoneRepository;

    public JobAndMilestoneData getJobAndMilestoneSummary(String email, String jobId) {

        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UUID validJobId;
        try {
            validJobId = UUID.fromString(jobId);
        } catch (IllegalArgumentException e) {
            // Throw a cleaner exception here. If you have a global exception handler,
            // you can map this to a 400 Bad Request instead of a 500.
            throw new RuntimeException("Invalid Job ID format. Must be a valid UUID.");
        }

        Jobs job = jobRepository.findById(validJobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!job.getFreelancer().getId().equals(user.getId())) {
            throw new RuntimeException("User is not the freelancer of this job!");
        }

        JobSummaryDto jobSummaryDto = jobRepository.findJobSummaryById(job.getId());
        List<MilestoneSummaryDto> milestoneSummaryDto = milestoneRepository.getMilestoneSummaryByJobId(job.getId());

        return new JobAndMilestoneData(jobSummaryDto, milestoneSummaryDto);
    }
}