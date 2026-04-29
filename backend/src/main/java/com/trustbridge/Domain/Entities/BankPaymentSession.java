package com.trustbridge.Domain.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.id.uuid.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "bank_payment_sessions")
public class BankPaymentSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_request_id", nullable = false)
    private PaymentRequest paymentRequest;

    @Column(name = "stripe_pi_id", length = 225 ,nullable = false, unique = true)
    private String stripePiId;

    @Column(name = "stripe_status", nullable = false)
    private String stripeStatus;

    @Column(name = "fallback_triggered", nullable = false)
    private Boolean fallbackTriggered;

    @Column(name = "fallback_trigger_source", nullable = false)
    private String fallbackTriggerSource;

    @Column(name = "actual_payment_method", nullable = false)
    private String actualPaymentMethod;

    @Column(name = "redirect_url", columnDefinition = "TEXT", nullable = false)
    private String redirectUrl;

}
