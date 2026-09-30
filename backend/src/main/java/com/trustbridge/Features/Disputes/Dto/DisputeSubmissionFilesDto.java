package com.trustbridge.Features.Disputes.Dto;

public record DisputeSubmissionFilesDto (
        String fileName,
        String storedPath,
        String contentType,
        long fileSize,
        String sha256Hash
) { }
