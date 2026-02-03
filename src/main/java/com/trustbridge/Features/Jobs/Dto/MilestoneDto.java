package com.trustbridge.Features.Jobs.Dto;

import java.math.BigDecimal;
import java.util.UUID;

public record MilestoneDto(
        UUID jobId,
        String title,
        BigDecimal amount,
        Integer sequence_amount
) {}
