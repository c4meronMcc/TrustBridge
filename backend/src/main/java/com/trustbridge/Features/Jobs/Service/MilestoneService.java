package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;

    /**
     * Creates and saves milestones for a given job based on the provided list of milestone DTOs.
     * If the list of DTOs is null or empty, the method exits without performing any action.
     *
     * @param jobs The job entity for which the milestones are being created. Must not be null.
     * @param dtos A list of milestone creation DTOs containing details such as title, amount,
     *             and sequence order for each milestone. If null or empty, no milestones are created.
     */
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
                        .status(MilestoneStatus.milestoneStatus.LOCKED)
                        .build()
                ).toList();

        milestoneRepository.saveAll(milestones);

        System.out.println("Saved "  + milestones.size() + " milestones for jobId: " + jobs.getId());
    }
}
