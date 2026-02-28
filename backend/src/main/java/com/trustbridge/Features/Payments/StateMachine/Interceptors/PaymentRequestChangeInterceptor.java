package com.trustbridge.Features.Payments.StateMachine.Interceptors;

import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestEvent;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PaymentRequestChangeInterceptor extends StateMachineInterceptorAdapter<PaymentRequestStatus, PaymentRequestEvent> {

    @Autowired
    PaymentRequestRepository paymentRequestRepository;

    @Override
    public void preStateChange(State<PaymentRequestStatus, PaymentRequestEvent> state,
                               Message<PaymentRequestEvent> message,
                               Transition<PaymentRequestStatus, PaymentRequestEvent> transition,
                               StateMachine<PaymentRequestStatus, PaymentRequestEvent> stateMachine,
                               StateMachine<PaymentRequestStatus, PaymentRequestEvent> rootStateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {

            UUID paymentRequestId = (UUID) msg.getHeaders().get("paymentRequestId");

            if (paymentRequestId != null) {
                PaymentRequest paymentRequest = paymentRequestRepository.findById(paymentRequestId)
                        .orElseThrow(() -> new RuntimeException("Payment Request not found!"));

                String initialState = paymentRequest.getStatus().name();

                paymentRequest.transitionToState(PaymentRequestStatus.valueOf(state.getId().name()));

                paymentRequestRepository.save(paymentRequest);

                System.out.println("Payment Request Status Changed From: " + initialState + " to: " + state.getId().name());
            }
        });
    }
}
