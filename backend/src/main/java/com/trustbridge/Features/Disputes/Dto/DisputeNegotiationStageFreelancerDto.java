package com.trustbridge.Features.Disputes.Dto;

import com.trustbridge.Domain.Entities.DisputeEvidenceFiles;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public record DisputeNegotiationStageFreelancerDto(
        String JobTitle,
        String MilestoneTitle,
        String DisputeId,
        String reason,
        List<DisputeSubmissionFileDto> files,
        BigDecimal clientProposedAmount,
        BigDecimal originalAmount
) {
}
