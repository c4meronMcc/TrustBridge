package com.trustbridge.Features.Payments.StateMachine;

import com.trustbridge.Domain.Enums.PaymentRequestEvent;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class PaymentRequestStateMachineConfig extends EnumStateMachineConfigurerAdapter<PaymentRequestStatus, PaymentRequestEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<PaymentRequestStatus, PaymentRequestEvent> states) throws Exception {
        states
                .withStates()
                .initial(PaymentRequestStatus.PENDING)
                .states(EnumSet.allOf(PaymentRequestStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<PaymentRequestStatus, PaymentRequestEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.PROCESSING)
                .event(PaymentRequestEvent.INITIATE_PAYMENT)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PROCESSING).target(PaymentRequestStatus.PAID)
                .event(PaymentRequestEvent.PAYMENT_SUCCESSFUL)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PROCESSING).target(PaymentRequestStatus.FAILED)
                .event(PaymentRequestEvent.PAYMENT_FAILED)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.FAILED)
                .event(PaymentRequestEvent.PAYMENT_FAILED)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.FAILED).target(PaymentRequestStatus.PENDING)
                .event(PaymentRequestEvent.RETRY_PAYMENT)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.EXPIRED)
                .event(PaymentRequestEvent.EXPIRE_REQUEST)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.CANCELLED)
                .event(PaymentRequestEvent.CANCEL_REQUEST)
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PAID).target(PaymentRequestStatus.REFUNDED)
                .event(PaymentRequestEvent.REFUND_REQUEST);
    }
}
