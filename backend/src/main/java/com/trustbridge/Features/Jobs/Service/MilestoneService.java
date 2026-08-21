package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.*;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.MilestoneSubmissionFileRepository;
import com.trustbridge.Domain.Repositories.MilestoneSubmissionRepository;
import com.trustbridge.Features.Jobs.Dto.JobCreationDto;
import com.trustbridge.Features.Notifications.Listeners.MilestoneEmailListener;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
import com.trustbridge.Features.Payments.Events.MilestoneSubmittedForApprovalEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneStateService milestoneStateService;
    private final MilestoneEmailListener milestoneEmailListener;
    private final FileStorageService fileStorageService;
    private final MilestoneSubmissionRepository milestoneSubmissionRepository;
    private final MilestoneSubmissionFileRepository milestoneSubmissionFileRepository;

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

        log.info("Saved {} milestones for jobId: {}", milestones.size(), jobs.getId());
    }

    public void freelancerSubmittedMilestone(UUID milestoneId, String deliverableLink, String notes, List<String> scopeItems, List<MultipartFile> files) throws IOException {

        Milestones milestone = milestoneRepository.findById(milestoneId).orElseThrow();

        if (milestone.getStatus() != MilestoneStatus.milestoneStatus.IN_PROGRESS) {
            throw new IllegalStateException("Milestone is not in SUBMITTED_FOR_APPROVAL state");
        }

        Users user = milestone.getJob().getFreelancer();

        MilestoneSubmissions submission = MilestoneSubmissions.builder()
                .milestone(milestone)
                .deliverableLink(deliverableLink)
                .notes(notes)
                .scopeItemsJson(scopeItems)
                .submittedBy(user)
                .build();

        milestoneSubmissionRepository.save(submission);

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String storedPath = fileStorageService.storeFile(file);

                    MilestoneSubmissionFile submissionFile = MilestoneSubmissionFile.builder()
                            .submission(submission)
                            .originalFilename(file.getOriginalFilename())
                            .storedPath(storedPath)
                            .contentType(file.getContentType())
                            .sizeBytes(file.getSize())
                            .build();

                    milestoneSubmissionFileRepository.save(submissionFile);
                }
            }
        }


        milestoneStateService.moveMilestoneIntoSubmission(milestoneId);

    }
}
