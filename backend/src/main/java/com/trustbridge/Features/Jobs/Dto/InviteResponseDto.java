package com.trustbridge.Features.Jobs.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.BigInteger;

public record InviteResponseDto(
    @NotNull String jobTitle,
    @NotNull String description,
    @NotNull String freelancerName,
    @NotNull String freelancerEmail,
    @NotNull BigDecimal amount,
    @NotNull String currency,
    @Nullable String clientEmail,
    @NotNull String userStatus
) { }
