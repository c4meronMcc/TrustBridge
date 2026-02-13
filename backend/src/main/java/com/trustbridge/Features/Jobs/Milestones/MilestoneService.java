package com.trustbridge.Features.Jobs.Milestones;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Jobs.JobService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class MilestoneService {

    MilestoneRepository milestoneRepository;
    JobService jobService;

    public MilestoneService(MilestoneRepository milestoneRepository, JobService jobService) {
        this.milestoneRepository = milestoneRepository;
        this.jobService = jobService;
    }

    @Transactional
    public void createMilestones(Jobs jobs, List<JobCreationDto.MilestoneCreationDto> dtos) {

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        List<Milestones> milestones = dtos.stream()
                .map(dto -> Milestones.builder()
                        .job(jobs)
                        .title(dto.title())
                        .amount(dto.amount())
                        .sequenceOrder(dto.sequence_amount())
                        .status(MilestoneStatus.milestoneStatus.PENDING)
                        .build()
                ).toList();

        milestoneRepository.saveAll(milestones);

        System.out.println("Saved "  + milestones.size() + " milestones for jobId: " + jobs.getId());
    }

    @Transactional
    public void changeStateToFunded(@Valid @RequestBody UUID milestoneId) {
        Milestones milestone = milestoneRepository.findById(milestoneId);
        milestone.setStatus(MilestoneStatus.milestoneStatus.FUNDED);
        milestoneRepository.save(milestone);
    }

    @Transactional
    public void changeStateToCompleted(@Valid @RequestBody UUID milestoneId) {
        Milestones milestone = milestoneRepository.findById(milestoneId);
        int numberOfMilestones = milestoneRepository.findAllByJobId(milestone.getJob().getId()).size();

        if (milestone.getSequenceOrder() == numberOfMilestones - 1) {
            milestone.setStatus(MilestoneStatus.milestoneStatus.FUNDED);
            milestoneRepository.save(milestone);
            jobService.jobStatusToComplete(milestone.getJob().getId());
        }
    }

    @Transactional
    public void changeMilestoneState(@Valid @RequestBody UUID milestoneId) {
        Milestones milestone = milestoneRepository.findById(milestoneId);

        List<MilestoneStatus.milestoneStatus> statuses = Arrays.asList(MilestoneStatus.milestoneStatus.values());




    }
}
