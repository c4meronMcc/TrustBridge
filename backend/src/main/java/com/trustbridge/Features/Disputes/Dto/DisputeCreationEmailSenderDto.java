package com.trustbridge.Features.Disputes.Dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record DisputeCreationEmailSenderDto(
   @NotNull String freelancerFirstName,
   @NotNull String freelancerEmail,
   @NotNull UUID milestoneId,
   @NotNull String milestoneTitle,
   @NotNull String description,
   @NotNull String currency,
   @NotNull BigDecimal amount,
   @NotNull BigDecimal proposedAmount,
   @NotNull String inviteToken
) {}
