package com.trustbridge.Features.Disputes.Service;

import com.trustbridge.Domain.Entities.*;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Repositories.*;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationEmailSenderDto;
import com.trustbridge.Features.Disputes.Dto.DisputeNegotiationStageFreelancerDto;
import com.trustbridge.Features.Disputes.Dto.DisputeSubmissionFileDto;
import com.trustbridge.Features.Jobs.Service.FileStorageService;
import com.trustbridge.Features.Jobs.Service.JobStateService;
import com.trustbridge.Features.Jobs.Service.MilestoneStateService;
import com.trustbridge.Features.Notifications.Listeners.DisputeEmailListener;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
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
    private final DisputeEmailListener  disputeEmailListener;

    /**
     * Processes a client dispute for a specific milestone. This method verifies the conditions
     * for creating a dispute, such as milestone status and existing disputes. If criteria are met,
     * it creates a new dispute, updates the dispute state, and notifies the relevant freelancer
     * via email.
     *
     * @param dto Data Transfer Object containing the necessary information to create a dispute.
     *            Includes details such as the milestone ID and the client-proposed amount.
     * @throws AccessDeniedException if:
     *         - A dispute already exists for the specified milestone.
     *         - The milestone is not in a valid state for dispute creation.
     *         - The user is not authorized to access the required resources.
     * @throws RuntimeException if an unexpected error occurs during dispute creation or notification processing.
     */
    @Transactional
    public void processClientDispute(DisputeCreationDto dto) {

        Boolean isDisputeActive = disputeRepository.findByMilestoneId(dto.milestoneId())
                .isPresent();

        if (isDisputeActive) {
            throw new AccessDeniedException("Dispute already exists for this milestone");
        }

        Milestones milestone = milestoneRepository.findById(dto.milestoneId())
                .orElseThrow(() -> new AccessDeniedException("Not authorized"));

        if (milestone.getStatus() == MilestoneStatus.SUBMITTED) {
            try {
                String disputeId = createNewDispute(dto);
                disputeStateService.disputeEvidenceSubmitted(dto.milestoneId());

                Users freelancer = userRepository.findByEmail(milestone.getJob().getFreelancer().getEmail())
                        .orElseThrow(() -> new AccessDeniedException("Not authorized"));

                DisputeCreationEmailSenderDto emailSenderDto = new DisputeCreationEmailSenderDto(
                        freelancer.getFirstName(),
                        freelancer.getEmail(),
                        milestone.getId(),
                        milestone.getTitle(),
                        milestone.getJob().getDescription(),
                        milestone.getJob().getCurrency(),
                        milestone.getAmount(),
                        dto.clientProposedAmount(),
                        generateInviteToken()
                );

                String inviteToken = generateInviteToken();

                disputeEmailListener.clientOpendedDispute(emailSenderDto, inviteToken, disputeId, freelancer);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            throw new AccessDeniedException("You are not authorized to create a dispute for this milestone");
        }

    }



    /**
     * Generates a random alphanumeric token to be used as an invite token.
     *
     * @return A 64-character long random alphanumeric string.
     */
    public String generateInviteToken() {
        return RandomStringUtils.randomAlphanumeric(64);
    }

    /**
     * Creates a new dispute based on the provided dispute creation details.
     *
     * @param dto a {@link DisputeCreationDto} object containing the necessary details
     *            to create a new dispute such as milestone ID, client proposed amount, etc.
     * @return a {@link String} representing the unique ID of the newly created dispute.
     * @throws RuntimeException if the milestone associated with the provided ID
     *                          does not exist.
     */
    @Transactional
    protected String createNewDispute(DisputeCreationDto dto) {

        Milestones milestone = milestoneRepository.findById(dto.milestoneId())
                .orElseThrow(() -> new RuntimeException("Milestone not found"));

        Dispute newDispute = Dispute.builder()
                .milestone(milestone)
                .clientProposedAmount(dto.clientProposedAmount())
                .finalSettlementCurrency(milestone.getJob().getCurrency())
                .build();

        addSubmissionToDispute(dto);

        disputeRepository.save(newDispute);

        return newDispute.getId().toString();
    }

    /**
     * Adds a new evidence submission to an existing dispute.
     * This method locates a dispute based on the milestone ID provided in the DTO,
     * creates a DisputeEvidenceSubmission entity associated with that dispute,
     * and saves it in the repository. It also processes and associates evidence files
     * to the submission.
     *
     * @param dto The data transfer object containing the dispute creation details,
     *            including milestone ID, reason, and evidence files.
     * @return The newly created DisputeEvidenceSubmission object.
     * @throws RuntimeException if no dispute is found for the given milestone ID.
     */
    @Transactional
    protected DisputeEvidenceSubmission addSubmissionToDispute(DisputeCreationDto dto) {

        Dispute dispute = disputeRepository.findByMilestoneId(dto.milestoneId())
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        DisputeEvidenceSubmission submission = DisputeEvidenceSubmission.builder()
                .dispute(dispute)
                .reason(dto.reason())
                .build();

        disputeEvidenceSubmissionRepository.save(submission);

        addDisputeEvidenceFiles(submission, dto.files());

        return submission;
    }

    /**
     * Adds evidence files to the specified dispute evidence submission.
     *
     * This method finds the dispute associated with the provided submission,
     * stores the provided evidence files, and then saves the stored file details
     * in the dispute evidence files repository.
     *
     * @param submission The dispute evidence submission to which the files are related.
     * @param files List of files to be added as evidence to the dispute. Each file must
     *              be non-empty for it to be processed and stored.
     */
    @Transactional
    protected void addDisputeEvidenceFiles(DisputeEvidenceSubmission submission, List<MultipartFile> files)  {

        Dispute dispute = disputeRepository.findByMilestoneId(submission.getDispute().getMilestone().getId())
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) { // could potentially fail if file is half full
                    String storedPath = null;
                    try {
                        storedPath = fileStorageService.storeFile(file);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

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

    public DisputeNegotiationStageFreelancerDto getDisputeSubmissionForFreelancer(String inviteToken) {

        DisputeEvidenceSubmission disputeEvidence = disputeEvidenceSubmissionRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new RuntimeException("Dispute Evidence not found"));

        List<DisputeSubmissionFileDto> files = disputeEvidenceFilesRepository.findAllBySubmissionId(disputeEvidence.getId())
                .stream()
                .map(file -> new DisputeSubmissionFileDto(
                        file.getId(),
                        file.getFileName(),
                        file.getSizeBytes(),
                        "/api/files/download/" + file.getId()
                ))
                .toList();

        return new DisputeNegotiationStageFreelancerDto(
                disputeEvidence.getDispute().getMilestone().getJob().getTitle(),
                disputeEvidence.getDispute().getMilestone().getTitle(),
                disputeEvidence.getDispute().getId().toString(),
                disputeEvidence.getReason(),
                files,
                disputeEvidence.getDispute().getClientProposedAmount(), // going to change this so that the proposed
                // amount is within the submission rather than the actual dispute
                disputeEvidence.getDispute().getMilestone().getAmount()
        );


    }

}
