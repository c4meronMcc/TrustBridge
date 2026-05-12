package com.trustbridge.Features.Payments.Service;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.PaymentMethodType;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Payments.Dto.PaymentRequestResponse;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
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

    @Transactional
    public PaymentRequestResponse createPaymentRequest(UUID milestoneId) throws Exception {

        Milestones milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new IllegalArgumentException("Milestone not found"));

        Optional<PaymentRequest> existingRequest = paymentRequestRepository
                .findByMilestoneIdAndStatus(milestoneId, PaymentRequestStatus.PENDING);

        PaymentRequest paymentRequest;

        if (existingRequest.isPresent() && existingRequest.get().getExpiresAt().isAfter(OffsetDateTime.now())) {
            paymentRequest = existingRequest.get();
        } else {
            paymentRequest = PaymentRequest.builder()
                    .milestone(milestone)
                    .amount(milestone.getAmount())
                    .status(PaymentRequestStatus.PENDING)
                    .expiresAt(OffsetDateTime.now().plusHours(24))
                    .build();
            paymentRequest = paymentRequestRepository.save(paymentRequest);
        }

        return switch (PaymentMethodType.fromAmount(milestone.getAmount())) {
            case STRIPE -> {
                String clientSecret = stripePaymentService.createSettlementIntent(paymentRequest);
                yield PaymentRequestResponse.stripe(paymentRequest.getId(), clientSecret);
            }
            case ESCROW -> throw new UnsupportedOperationException("Escrow.com integration not yet implemented");
        };
    }
}
