package com.trustbridge.Features.Payments.Service;

import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestEvent;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
import com.trustbridge.Features.Payments.StateMachine.Interceptors.PaymentRequestChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentStateService {

    private final PaymentRequestRepository paymentRequestRepository;

    @Autowired
    StateMachineFactory<PaymentRequestStatus, PaymentRequestEvent> stateMachineFactory;

    @Autowired
    PaymentRequestChangeInterceptor interceptor;

    @Autowired
    ApplicationEventPublisher eventPublisher;

    private StateMachine<PaymentRequestStatus, PaymentRequestEvent> buildStateMachine(UUID paymentId) {

        PaymentRequest paymentRequest = paymentRequestRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found!"));

        StateMachine<PaymentRequestStatus, PaymentRequestEvent> sm = stateMachineFactory.getStateMachine(paymentId.toString());

        sm.stopReactively().block();

        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {

            accessor.addStateMachineInterceptor(interceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    paymentRequest.getStatus(), null, null, null
            )).block();
        });

        sm.startReactively().block();

        return sm;
    }

    public void fireEvent(UUID paymentId, PaymentRequestEvent event, String guardKey) {
        StateMachine<PaymentRequestStatus, PaymentRequestEvent> sm = buildStateMachine(paymentId);

        Message<PaymentRequestEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("paymentRequestId", paymentId)
                .setHeader(guardKey, true)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();

        sm.sendEvent(Mono.just(message)).subscribe(result -> {
            if (result.getResultType() == org.springframework.statemachine.StateMachineEventResult.ResultType.DENIED) {
                System.out.println("❌ TRANSITION DENIED! Event: " + event + " | Current DB State: " + sm.getState().getId());
            } else if (result.getResultType() == org.springframework.statemachine.StateMachineEventResult.ResultType.ACCEPTED) {
                System.out.println("✅ TRANSITION ACCEPTED! Event: " + event);
            }
        });
    }

    public void initiatePayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.INITIATE_PAYMENT, "isPaymentInitiated");
    }

    public void paymentSuccessful(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.PAYMENT_SUCCESSFUL, "isPaymentSuccessful");
    }

    public void paymentFailed(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.PAYMENT_FAILED, "isPaymentFailed");
    }

    public void retryPayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.RETRY_PAYMENT, "isPaymentRetrying");
    }

    public void expirePayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.EXPIRE_REQUEST, "isPaymentExpired");
    }

    public void cancelPayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.CANCEL_REQUEST, "isPaymentCancelled");
    }

    public void refundPayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.REFUND_REQUEST, "isPaymentRefunded");
    }

    public void confirmFunded(UUID paymentRequestId, UUID milestoneId) {
        paymentSuccessful(paymentRequestId);
        eventPublisher.publishEvent(new MilestoneFundedEvent(this, milestoneId));
    }
}
