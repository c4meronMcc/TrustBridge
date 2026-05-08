package com.trustbridge.Features.Payments.Dto;

import com.trustbridge.Domain.Enums.StripeWebhookProcessingStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WebhookLogResponse(
        UUID id,
        String stripeEventId,
        String eventType,
        StripeWebhookProcessingStatus processingStatus,
        String errorMessage,
        OffsetDateTime createdAt
) {}
