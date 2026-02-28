package com.trustbridge.Features.Payments.Service;

import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestEvent;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Payments.StateMachine.Interceptors.PaymentRequestChangeInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class PaymentStateService {


    PaymentRequestRepository paymentRequestRepository;

    @Autowired
    StateMachineFactory<PaymentRequestStatus, PaymentRequestEvent> stateMachineFactory;

    @Autowired
    PaymentRequestChangeInterceptor interceptor;

    public PaymentStateService(PaymentRequestRepository paymentRequestRepository) {
        this.paymentRequestRepository = paymentRequestRepository;
    }


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

    public void fireEvent(UUID paymentId, PaymentRequestEvent event) {
        StateMachine<PaymentRequestStatus, PaymentRequestEvent> sm = buildStateMachine(paymentId);

        Message<PaymentRequestEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("paymentRequestId", paymentId)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
    }

    public void initiatePayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.INITIATE_PAYMENT);
    }

    public void paymentSuccessful(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.PAYMENT_SUCCESSFUL);
    }

    public void paymentFailed(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.PAYMENT_FAILED);
    }

    public void retryPayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.RETRY_PAYMENT);
    }

    public void expirePayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.EXPIRE_REQUEST);
    }

    public void cancelPayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.CANCEL_REQUEST);
    }

    public void refundPayment(UUID paymentId) {
        fireEvent(paymentId, PaymentRequestEvent.REFUND_REQUEST);
    }

}
