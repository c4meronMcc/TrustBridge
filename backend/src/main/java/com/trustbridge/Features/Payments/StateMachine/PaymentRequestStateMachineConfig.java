package com.trustbridge.Features.Payments.StateMachine;

import com.trustbridge.Domain.Enums.PaymentRequestEvent;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

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
                .guard(isPaymentInitiatedGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PROCESSING).target(PaymentRequestStatus.PAID)
                .event(PaymentRequestEvent.PAYMENT_SUCCESSFUL)
                .guard(isPaymentSuccessfulGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PROCESSING).target(PaymentRequestStatus.FAILED)
                .event(PaymentRequestEvent.PAYMENT_FAILED)
                .guard(isPaymentFailedGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.FAILED)
                .event(PaymentRequestEvent.PAYMENT_FAILED)
                .guard(isPaymentRetryingGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.FAILED).target(PaymentRequestStatus.PENDING)
                .event(PaymentRequestEvent.RETRY_PAYMENT)
                .guard(isPaymentExpiredGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.EXPIRED)
                .event(PaymentRequestEvent.EXPIRE_REQUEST)
                .guard(isPaymentCancelledGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PENDING).target(PaymentRequestStatus.CANCELLED)
                .event(PaymentRequestEvent.CANCEL_REQUEST)
                .guard(isPaymentRefundedGuard())
                .and()
                .withExternal()
                .source(PaymentRequestStatus.PAID).target(PaymentRequestStatus.REFUNDED)
                .event(PaymentRequestEvent.REFUND_REQUEST)
                .guard(isPaymentRefundedGuard());
    }

    // TODO: Develop all core logic for guarded states so that states are actually guarded and not just changed automatically


    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentInitiatedGuard() {
        return context -> {
            Boolean isPaymentInitiated = context.getMessageHeaders().get("isPaymentInitiated", Boolean.class);
            return isPaymentInitiated != null && isPaymentInitiated;
        };
    }

    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentSuccessfulGuard() {
        return context -> {
            Boolean isPaymentSuccessful = context.getMessageHeaders().get("isPaymentSuccessful", Boolean.class);
            return isPaymentSuccessful != null && isPaymentSuccessful;
        };
    }

    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentFailedGuard() {
        return context -> {
            Boolean isPaymentFailed = context.getMessageHeaders().get("isPaymentFailed", Boolean.class);
            return isPaymentFailed != null && isPaymentFailed;
        };
    }

    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentRetryingGuard() {
        return context -> {
            Boolean isPaymentRetrying = context.getMessageHeaders().get("isPaymentRetrying", Boolean.class);
            return isPaymentRetrying != null && isPaymentRetrying;
        };
    }

    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentExpiredGuard() {
        return context -> {
            Boolean isPaymentExpired = context.getMessageHeaders().get("isPaymentExpired", Boolean.class);
            return isPaymentExpired != null && isPaymentExpired;
        };
    }

    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentCancelledGuard() {
        return context -> {
            Boolean isPaymentCancelled = context.getMessageHeaders().get("isPaymentCancelled", Boolean.class);
            return isPaymentCancelled != null && isPaymentCancelled;
        };
    }

    @Bean
    public Guard<PaymentRequestStatus, PaymentRequestEvent> isPaymentRefundedGuard() {
        return context -> {
            Boolean isPaymentRefunded = context.getMessageHeaders().get("isPaymentRefunded", Boolean.class);
            return isPaymentRefunded != null && isPaymentRefunded;
        };
    }
}
