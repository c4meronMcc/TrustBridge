package com.trustbridge.Domain.Entities;

import com.trustbridge.Domain.Enums.StripeWebhookProcessingStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "stripe_webhook_logs")
public class StripeWebhookLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "stripe_event_id", nullable = false)
    private String stripeEventId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_request_id", nullable = false)
    private PaymentRequest paymentRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status", nullable = false)
    private StripeWebhookProcessingStatus processingStatus;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
}
