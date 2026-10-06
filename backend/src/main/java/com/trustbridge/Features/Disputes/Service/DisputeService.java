package com.trustbridge.Features.Disputes.Service;

import com.trustbridge.Domain.Entities.*;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.*;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import com.trustbridge.Features.Disputes.Dto.DisputeSubmissionFilesDto;
import com.trustbridge.Features.Jobs.Service.FileStorageService;
import com.trustbridge.Features.Jobs.Service.JobService;
import com.trustbridge.Features.Jobs.Service.JobStateService;
import com.trustbridge.Features.Jobs.Service.MilestoneStateService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
    private final DisputeStateService disputeStateService;
    private final MilestoneStateService milestoneStateService;
    private final JobStateService jobStateService;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;
    private final DisputeEvidenceSubmissionRepository disputeEvidenceSubmissionRepository;
    private final DisputeEvidenceFilesRepository disputeEvidenceFilesRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public void processClientDispute(DisputeCreationDto dto, String authenicatedEmail, List<MultipartFile> files) throws IOException {

        Boolean isDisputeActive = disputeRepository.findByMilestoneId(UUID.fromString(dto.milestoneId()))
                .isPresent();

        if (isDisputeActive) {
            throw new AccessDeniedException("Dispute already exists for this milestone");
        }

        Milestones milestone = milestoneRepository.findById(UUID.fromString(dto.milestoneId()))
                .orElseThrow(() -> new AccessDeniedException("Milestone not found"));

        if (milestone.getJob().getClient().getEmail().equals(authenicatedEmail) && milestone.getStatus() == MilestoneStatus.SUBMITTED) {
            createNewDispute(dto);
            DisputeEvidenceSubmission submission = addSubmissionToDispute(dto);
            addDisputeEvidenceFiles(dto, submission, files);

            disputeStateService.moveDisputeIntoSubmission(submission.getDispute().getId());
            milestoneStateService.disputeRaised(submission.getDispute().getMilestone().getId());
            jobStateService.raiseDispute(submission.getDispute().getMilestone().getJob().getId());

        } else {
            throw new AccessDeniedException("You are not authorized to create a dispute for this milestone");
        }

    }

    @Transactional
    public void createNewDispute(DisputeCreationDto dto) {

        Milestones milestone = milestoneRepository.findById(UUID.fromString(dto.milestoneId()))
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        Dispute newDispute = Dispute.builder()
                .milestone(milestone)
                .clientProposedAmount(dto.clientProposedAmount())
                .finalSettlementCurrency(milestone.getJob().getCurrency())
                .build();

        disputeRepository.save(newDispute);
    }

    @Transactional
    public DisputeEvidenceSubmission addSubmissionToDispute(DisputeCreationDto dto) {

        Dispute dispute = disputeRepository.findByMilestoneId(UUID.fromString(dto.milestoneId()))
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        DisputeEvidenceSubmission submission = DisputeEvidenceSubmission.builder()
                .dispute(dispute)
                .reason(dto.reason())
                .build();

        disputeEvidenceSubmissionRepository.save(submission);

        return submission;
    }

    @Transactional
    public void addDisputeEvidenceFiles(DisputeCreationDto dto, DisputeEvidenceSubmission submission, List<MultipartFile> files) throws IOException {

        Dispute dispute = disputeRepository.findByMilestoneId(UUID.fromString(dto.milestoneId()))
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String storedPath = fileStorageService.storeFile(file);

                    DisputeEvidenceFiles submissionFile =DisputeEvidenceFiles.builder()
                            .submission(submission)
                            .fileName(file.getOriginalFilename())
                            .storedPath(storedPath)
                            .contentType(file.getContentType())
                            .sizeBytes(file.getSize())
                            .build();
                    disputeEvidenceFilesRepository.save(submissionFile);
                }
            }
        }
    }

}
