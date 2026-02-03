package com.trustbridge.Features.Jobs.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record JobCreationDto(
        @NotNull String freelancerEmail,
        @Nullable String clientEmail,
        @Nullable String clientPhoneNumber,
        @Nullable String clientFirstName,
        @Nullable String clientLastName,
        @NotNull String title,
        @NotNull String description,
        @NotNull @Positive BigDecimal amount,
        @NotNull String currency,
        @NotNull String countryCode,
        @Nullable List<MilestoneCreationDto> milestones
        )
{
    public record MilestoneCreationDto(
            @NotNull String title,
            @NotNull @Positive BigDecimal amount,
            @NotNull Integer sequence_amount
    ){}
}
