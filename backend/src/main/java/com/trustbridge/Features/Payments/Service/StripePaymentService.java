package com.trustbridge.Features.Payments.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentLinkCreateParams;
import com.trustbridge.Domain.Entities.*;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Repositories.BankPaymentSessionRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Domain.Repositories.StripeCustomerRepository;
import com.trustbridge.Features.Jobs.Service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor // 1. Lombok handles the constructor automatically
public class StripePaymentService {

    private final StripeCustomerRepository stripeCustomerRepository;
    private final EmailService emailService;
    private final PaymentRequestRepository paymentRequestRepository; // 2. ADDED 'final'
    private final BankPaymentSessionRepository bankPaymentSessionRepository;

    // Notice: The manual constructor is completely GONE.

    public void validatePaymentRequest(PaymentRequestStatus paymentRequestStatus) {
        switch (paymentRequestStatus) {
            case PAID -> throw new IllegalStateException("Payment request is already paid");
            case EXPIRED -> throw new IllegalStateException("Payment request has expired");
            case REFUNDED -> throw new IllegalStateException("Payment request has been refunded");
            case FAILED -> log.info("Payment request has failed previously, allowing retry");
            default -> log.info("Payment request is valid for checkout");
        }
    }

    public void savePaymentIntentSession(PaymentIntent paymentIntent, PaymentRequest paymentRequest) {
        BankPaymentSession session = bankPaymentSessionRepository.findByPaymentRequestId(paymentRequest.getId())
                .orElse(new BankPaymentSession());

        session.setPaymentRequest(paymentRequest);
        session.setStripePiId(paymentIntent.getId());
        session.setStripeStatus(paymentIntent.getStatus());
        session.setRedirectUrl("PENDING");

        bankPaymentSessionRepository.save(session);
    }

    public String createSettlementIntent(PaymentRequest paymentRequest) throws StripeException {
        validatePaymentRequest(paymentRequest.getStatus());

        Long amountInPence = paymentRequest.getAmount().multiply(new BigDecimal("100")).longValueExact();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPence)
                .setCurrency(paymentRequest.getMilestone().getJob().getCurrency())
                .addPaymentMethodType(PaymentLinkCreateParams.PaymentMethodType.PAY_BY_BANK.getValue())
                .addPaymentMethodType(PaymentLinkCreateParams.PaymentMethodType.CARD.getValue())
                .putMetadata("payment_request_id", paymentRequest.getId().toString())
                .putMetadata("milestone_id", paymentRequest.getMilestone().getId().toString())
                .putMetadata("fallback_tracking", "enabled")
                .putMetadata("fallback_trigger_source", "settlement_intent")
                .build();

        RequestOptions requestOptions = RequestOptions.builder()
                .setIdempotencyKey(paymentRequest.getId().toString())
                .build();

        PaymentIntent intent = PaymentIntent.create(params, requestOptions);
        savePaymentIntentSession(intent, paymentRequest);

        return intent.getClientSecret();
    }

    public String getOrCreateStripeCustomer(Users client) throws StripeException {
        Optional<StripeCustomer> existing = stripeCustomerRepository.findByUserId(client.getId());
        if (existing.isPresent()) {
            return existing.get().getStripeCustomerId();
        }

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(client.getFirstName() + " " + client.getLastName())
                .setEmail(client.getEmail())
                .build();

        Customer stripeCustomer = Customer.create(params);

        StripeCustomer entity = StripeCustomer.builder()
                .user(client)
                .stripeCustomerId(stripeCustomer.getId())
                .build();
        stripeCustomerRepository.save(entity);

        return stripeCustomer.getId();
    }
}