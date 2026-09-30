package com.trustbridge.Features.Disputes.Dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record DisputeCreationDto(
        @NotNull String milestoneId,
        @Positive BigDecimal clientProposedAmount,
        @NotNull String reason,
        @NotNull String
        ) {}
