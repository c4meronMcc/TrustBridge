package com.trustbridge.Domain.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.PaymentRequestStatus.*;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter(AccessLevel.PROTECTED)
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_requests")
public class PaymentRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "milestone_id", nullable = false)
    private Milestones milestone;

    @Getter(AccessLevel.NONE)
    @JsonIgnore
    @Column(name = "payment_link_token", nullable = false)
    private UUID paymentLinkToken;

    @Column(name = "amount", precision = 19, scale = 4, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentRequestStatus status;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

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

    // Package-private getter for service layer
    UUID getPaymentLinkTokenInternal() {
        return paymentLinkToken;
    }

    public void transitionToState(PaymentRequestStatus newStatus) {
        // You can even add a quick safety check here if you want!
        if (newStatus == null) {
            throw new IllegalArgumentException("Cannot transition to a null state");
        }
        this.status = newStatus;
    }
}