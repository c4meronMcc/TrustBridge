package com.trustbridge.Features.Payments.Events;

import com.trustbridge.Domain.Enums.PaymentRequestEvent;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class PaymentRequestCreatedEvent extends ApplicationEvent {
    private final UUID milestoneId;
    private final UUID paymentRequestId;

    public PaymentRequestCreatedEvent(Object source, UUID milestoneId, UUID paymentRequestId) {
        super(source);
        this.milestoneId = milestoneId;
        this.paymentRequestId = paymentRequestId;
    }

    public UUID getMilestoneId() {
        return milestoneId;
    }

    public UUID getPaymentRequestId() {
        return paymentRequestId;
    }
}
