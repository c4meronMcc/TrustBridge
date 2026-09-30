package com.trustbridge.Features.Disputes.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record DisputeCreationDto(
        @NotNull String milestoneId,
        @Positive BigDecimal clientProposedAmount,
        @NotNull String reason,

        @NotNull List<DisputeSubmissionFilesDto> files
) { }
