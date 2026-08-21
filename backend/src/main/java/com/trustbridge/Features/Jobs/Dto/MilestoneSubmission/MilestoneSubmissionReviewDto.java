package com.trustbridge.Features.Jobs.Dto.MilestoneSubmission;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MilestoneSubmissionReviewDto(
        UUID submissionId,
        UUID milestoneId,
        String submittedBy,
        String notes,
        String deliverableLink,
        List<ScopeItemDto> scopeItems, // Parsed server-side from scopeItemsJson
        List<SubmissionFileDto> files
) { }
