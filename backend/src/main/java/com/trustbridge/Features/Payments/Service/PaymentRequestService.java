package com.trustbridge.Features.Payments.Service;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.PaymentMethodType;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Features.Jobs.Dto.PaymentActivationDto;
import com.trustbridge.Features.Payments.Provider.Dto.SettlementResult;
import com.trustbridge.Features.Payments.Provider.PaymentGateway;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentRequestService {

    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestRepository paymentRequestRepository;
    private final PaymentGateway paymentGateway;

    @Transactional
    public void createPaymentRequest(Milestones milestone) throws Exception {

        Optional<PaymentRequest> existingRequest = paymentRequestRepository
                .findByMilestoneIdAndStatus(milestone.getId(), PaymentRequestStatus.PENDING);

        if (existingRequest.isPresent() && existingRequest.get().getExpiresAt().isAfter(OffsetDateTime.now())) {
            return;
        }

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .milestone(milestone)
                .amount(milestone.getAmount())
                .status(PaymentRequestStatus.PENDING)
                .paymentMethodType(PaymentMethodType.fromAmount(milestone.getAmount()))
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();

        paymentRequest = paymentRequestRepository.save(paymentRequest);

        switch (paymentRequest.getPaymentMethodType()) {
            case STRIPE -> {
                SettlementResult result = paymentGateway.createSettlementIntent(paymentRequest);

                paymentRequest.setToken(result.token());

                paymentRequestRepository.save(paymentRequest);
            }
            case ESCROW -> {
                throw new UnsupportedOperationException("Escrow.com integration not yet implemented");
            }
        }
    }

    public String getClientSecretForMilestone(UUID milestoneId) {
        PaymentRequest request = paymentRequestRepository
                .findByMilestoneIdAndStatus(milestoneId, PaymentRequestStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No active payment request found for this milestone."));

        return request.getToken();
    }

    /**
     * Retrieves the payment activation details for a specified milestone.
     *
     * @param milestoneId the unique identifier of the milestone associated with the payment request
     * @return a {@code PaymentActivationDto} containing the client secret, payment request ID,
     *         and the payment provider name for the active payment request
     * @throws RuntimeException if no active payment request is found for the specified milestone
     */
    public PaymentActivationDto getPaymentActivationDetails(UUID milestoneId) {
        PaymentRequest request = paymentRequestRepository
                .findByMilestoneIdAndStatus(milestoneId, PaymentRequestStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No active payment request found for this milestone."));

        return new PaymentActivationDto(
                request.getToken(),
                request.getId().toString(),
                paymentGateway.getProviderName()
        );
    }
}