package com.trustbridge.Features.Payments.Service;

import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.stripe.net.ApiResource;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.Service.MilestoneStateService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class StripeWebhookService {

    private final MilestoneRepository milestoneRepository;
    private final MilestoneStateService milestoneStateService;

    public StripeWebhookService(MilestoneRepository milestoneRepository, MilestoneStateService milestoneStateService) {
        this.milestoneRepository = milestoneRepository;
        this.milestoneStateService = milestoneStateService;
    }

    public void handleCheckoutSessionCompleted(String rawJson) {
        Session session = ApiResource.GSON.fromJson(rawJson, Session.class);
        String milestoneIdStr = session.getMetadata().get("milestone_id");

        if (milestoneIdStr == null) {
            System.err.println("CRITICAL: Session " + session.getId() + " missing milestone_id metadata.");
            return;
        }

        processMilestonePayment(milestoneIdStr);
    }

    public void handlePaymentIntentSucceeded(String rawJson) {
        PaymentIntent paymentIntent = ApiResource.GSON.fromJson(rawJson, PaymentIntent.class);

        if (!"customer_balance".equals(paymentIntent.getPaymentMethod())) {
            return;
        }

        String milestoneIdStr = paymentIntent.getMetadata().get("milestone_id");

        if (milestoneIdStr == null) {
            System.err.println("CRITICAL: PaymentIntent " + paymentIntent.getId() + " missing milestone_id metadata.");
            return;
        }

        processMilestonePayment(milestoneIdStr);
    }

    private void processMilestonePayment(String milestoneIdStr) {
        Optional<Milestones> milestoneOpt = milestoneRepository.findById(UUID.fromString(milestoneIdStr));
        if (milestoneOpt.isEmpty()) {
            System.err.println("CRITICAL: Milestone " + milestoneIdStr + " not found.");
            return;
        }

        milestoneStateService.fundsDeposited(milestoneOpt.get().getId());
        System.out.println("Funds deposited for milestone: " + milestoneIdStr);
    }
}