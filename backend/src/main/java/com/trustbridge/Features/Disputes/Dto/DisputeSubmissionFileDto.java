package com.trustbridge.Features.Disputes.Dto;

import java.util.UUID;

public record DisputeSubmissionFileDto(
        UUID fileId,
        String fileName,
        long fileSizeBytes,
        String downloadUrl
) {
}
