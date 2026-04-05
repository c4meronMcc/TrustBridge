package com.trustbridge.Features.Payments.Service;

import com.stripe.model.PaymentIntent;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.PaymentMethodType;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Payments.Dto.PaymentRequestResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class PaymentRequestService {

    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final StripePaymentService stripePaymentService;

    public PaymentRequestService(MilestoneRepository milestoneRepository, PaymentRequestRepository paymentRequestRepository, StripePaymentService stripePaymentService) {
        this.milestoneRepository = milestoneRepository;
        this.paymentRequestRepository = paymentRequestRepository;
        this.stripePaymentService = stripePaymentService;
    }

    public PaymentRequestResponse createPaymentRequest(UUID milestoneId) throws Exception {

        Milestones milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new Exception("Milestone not found"));

        Users client = milestone.getJob().getClient();

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .milestone(milestone)
                .amount(milestone.getAmount())
                .status(PaymentRequestStatus.PENDING)
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();

        paymentRequest = paymentRequestRepository.save(paymentRequest);

        return switch (PaymentMethodType.fromAmount(milestone.getAmount())) {
            case CARD -> {
                PaymentIntent paymentIntent = stripePaymentService.createCardPaymentIntent(milestone, client);
                yield PaymentRequestResponse.card(paymentRequest.getId(), paymentIntent.getClientSecret());
            }
            case VIBAN -> {
                PaymentIntent paymentIntent = stripePaymentService.createVibansPaymentIntent(milestone, client);
                stripePaymentService.sendVibanEmail(client, paymentIntent, milestone);
                yield  PaymentRequestResponse.viban(paymentRequest.getId());
            }
            case ESCROW -> throw new UnsupportedOperationException("Escrow.com integration not yet implemented");
        };
    }
}
