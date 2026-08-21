package com.trustbridge.Features.Jobs.Dto.MilestoneSubmission;

import java.util.UUID;

public record SubmissionFileDto(
        UUID fileId,
        String fileName,
        long fileSizeBytes,
        String downloadUrl
) { }
