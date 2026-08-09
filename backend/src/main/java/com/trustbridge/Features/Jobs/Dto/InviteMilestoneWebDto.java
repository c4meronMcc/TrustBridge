package com.trustbridge.Features.Jobs.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record InviteMilestoneWebDto(
        @NotNull String id,
        @NotNull String title,
        @NotNull BigDecimal amount,
        @Nullable Integer sequence_amount
) {
}
