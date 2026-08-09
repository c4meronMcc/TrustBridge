package com.trustbridge.Features.Jobs.Dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List; // 🚨 Make sure to import this!

public record InviteResponseWebDto(
        @NotNull String title,          // Swapped from jobTitle so frontend reads it automatically
        @NotNull String description,
        @NotNull String clientName,
        @NotNull String freelancerName,
        @NotNull String freelancerEmail,
        @NotNull BigDecimal amount,
        @NotNull String currency,
        @Nullable String clientEmail,
        @NotNull String status,         // Swapped from userStatus to match frontend

        @NotNull List<InviteMilestoneWebDto> milestones // ✨ The missing piece that draws the timeline!
) {}

