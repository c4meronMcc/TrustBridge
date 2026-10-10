package com.trustbridge.Features.Disputes.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DisputeCreationDto(
        @NotNull UUID milestoneId,
        @NotNull @Positive BigDecimal clientProposedAmount,
        @NotNull @Size(max = 2000) String reason
) { }
