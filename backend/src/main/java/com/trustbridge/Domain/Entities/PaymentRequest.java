package com.trustbridge.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trustbridge.Domain.Enums.PaymentMethodType;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter // 👈 FIX 3: Removed PROTECTED so your PaymentRequestService can inject Stripe data!
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_requests")
@Builder
public class PaymentRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestones milestone;

    @Getter(AccessLevel.NONE)
    @JsonIgnore
    @Column(name = "payment_link_token", nullable = false, updatable = false)
    private UUID paymentLinkToken;

    @Column(name = "stripe_session_id")
    private String stripeSessionId;

    // 👈 FIX 2: Renamed to standard Java conventions to match the database column
    @Column(name = "token")
    private String token;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentRequestStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method_type", length = 50, nullable = false)
    private PaymentMethodType paymentMethodType;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    // 👈 FIX 1: This annotation forces Hibernate to run this BEFORE saving to Postgres!
    protected void onCreate() {
        if (paymentLinkToken == null) {
            paymentLinkToken = UUID.randomUUID();
        }
        if (expiresAt == null) {
            expiresAt = OffsetDateTime.now().plusDays(7);
        }
        if (status == null) {
            status = PaymentRequestStatus.PENDING;
        }
    }

    public void transitionToState(PaymentRequestStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Cannot transition to a null state");
        }
        this.status = newStatus;
    }
}