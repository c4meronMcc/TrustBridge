package com.trustbridge.Features.Dashboard.Freelancer.Dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AuditLogEntryDto(
        String eventId,
        String eventType,
        String description,
        BigDecimal amountGbp,
        String relatedPartyName,
        OffsetDateTime occurredAt
) {}