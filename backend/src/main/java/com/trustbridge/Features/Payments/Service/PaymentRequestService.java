package com.trustbridge.Features.Payments.Service;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Enums.PaymentRequestStatus;
import com.trustbridge.Domain.Enums.PaymentMethodType;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
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

    // ─── 1. THE STATE MACHINE ACTION (THE CREATOR) ───────────────────────────
    @Transactional
    public void createPaymentRequest(Milestones milestone) throws Exception {

        Optional<PaymentRequest> existingRequest = paymentRequestRepository
                .findByMilestoneIdAndStatus(milestone.getId(), PaymentRequestStatus.PENDING);

        // If a valid request already exists, we do nothing. The State Machine is happy.
        if (existingRequest.isPresent() && existingRequest.get().getExpiresAt().isAfter(OffsetDateTime.now())) {
            return;
        }

        // 1. Create the base database record first
        PaymentRequest paymentRequest = PaymentRequest.builder()
                .milestone(milestone)
                .amount(milestone.getAmount())
                .status(PaymentRequestStatus.PENDING)
                .paymentMethodType(PaymentMethodType.fromAmount(milestone.getAmount()))
                .expiresAt(OffsetDateTime.now().plusHours(24))
                .build();

        // 2. Save it immediately so it generates a UUID
        // (Stripe needs this ID to know what it's paying for!)
        paymentRequest = paymentRequestRepository.save(paymentRequest);

        // 3. Talk to the payment provider and save the "Secret Tickets" to the database
        switch (paymentRequest.getPaymentMethodType()) {
            case STRIPE -> {
                // Call Stripe server-to-server
                String clientSecret = stripePaymentService.createSettlementIntent(paymentRequest);

                // Save the cryptographic ticket into the PostgreSQL row
                paymentRequest.setToken(clientSecret);

                // (Optional but recommended: If your Stripe service returns the 'pi_123' Intent ID,
                // you should save it to paymentRequest.setStripeSessionId(...) here too for the webhook later!)

                paymentRequestRepository.save(paymentRequest);
            }
            case ESCROW -> {
                throw new UnsupportedOperationException("Escrow.com integration not yet implemented");
            }
        }
    }

    // ─── 2. THE CONTROLLER FETCHER (THE RETRIEVER) ───────────────────────────
    // This is the method your JobService uses to hand the secret to Next.js!
    public String getClientSecretForMilestone(UUID milestoneId) {
        PaymentRequest request = paymentRequestRepository
                .findByMilestoneIdAndStatus(milestoneId, PaymentRequestStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("No active payment request found for this milestone."));

        return request.getToken(); // Hands back the "pi_3M..._secret_XYZ" ticket
    }
}