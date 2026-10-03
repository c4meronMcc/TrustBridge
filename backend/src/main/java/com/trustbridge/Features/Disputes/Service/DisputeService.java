package com.trustbridge.Features.Disputes.Service;

import com.trustbridge.Domain.Entities.*;
import com.trustbridge.Domain.Repositories.DisputeEvidenceSubmissionRepository;
import com.trustbridge.Domain.Repositories.DisputeRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Disputes.Dto.DisputeCreationDto;
import com.trustbridge.Features.Disputes.Dto.DisputeSubmissionFilesDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeRepository disputeRepository;
//    private final DisputeStateService disputeStateService;
    private final UserRepository userRepository;
    private final MilestoneRepository milestoneRepository;
    private final DisputeEvidenceSubmissionRepository disputeEvidenceSubmissionRepository;

    public void processClientDispute(DisputeCreationDto dto, String authenicatedEmail, List<MultipartFile> files) {

        Boolean userAuth = userRepository.findByEmail(authenicatedEmail).isPresent();

        if (userAuth) {

        }

        if (userAuth) {
            createNewDispute(dto);
            DisputeEvidenceSubmission submission = addSubmissionToDispute(dto);
            addDisputeEvidenceFiles(dto, submission, files);
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
    public void addDisputeEvidenceFiles(DisputeCreationDto dto, DisputeEvidenceSubmission submission, List<MultipartFile> files) {

        Dispute dispute = disputeRepository.findByMilestoneId(UUID.fromString(dto.milestoneId()))
                .orElseThrow(() -> new RuntimeException("Dispute not found"));

        /*
        * if (files != null && !files.isEmpty()) {
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
        * */

        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String storedPath = file.getOriginalFilename();

                    DisputeEvidenceFiles.builder()
                            .submission(submission)
                            .fileName(file.getOriginalFilename())
                            .storedPath(storedPath)
                            .contentType(file.getContentType())
                            .sizeBytes(file.getSize())
                            .build();
                    disputeEvidenceSubmissionRepository.save(submission);
                }
            }
        }





    }

}
