package com.trustbridge.Features.Payments.Service;

import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.StripeWebhookLogs;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Enums.StripeWebhookProcessingStatus;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.StripeWebhookLogsRepository;
import com.trustbridge.Features.Notifications.Listeners.PaymentEmailListener;
import com.trustbridge.Features.Payments.Events.MilestoneFundedEvent;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    private final StripeWebhookLogsRepository webhookLogsRepository;
    private final MilestoneRepository milestoneRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentEmailListener paymentEmailListener;

    @Transactional
    public void handlePaymentIntentSucceeded(Event event) {
        String eventId = event.getId();

        if (webhookLogsRepository.existsByStripeEventId(eventId)) {
            log.info("Idempotency trigger: Webhook event {} has already been processed. Ignoring.", eventId);
            return;
        }

        try {
            Optional<PaymentIntent> paymentIntentOptional = event.getDataObjectDeserializer().getObject()
                    .map(obj -> (PaymentIntent) obj);

            if (paymentIntentOptional.isEmpty()) {
                throw new IllegalStateException("Deserialisation failed. payload is nto a PaymentIntent.");
            }

            PaymentIntent paymentIntent = paymentIntentOptional.get();

            String milestoneIdStr = paymentIntent.getMetadata().get("milestone_id");
            if (milestoneIdStr == null) {
                log.warn("Webhook event {} has no milestone_id in metadata. Cannot route funds.", eventId);
                saveAuditLog(eventId, "payment_intent.succeeded", StripeWebhookProcessingStatus.FAILED.name(), "No milestone_id in metadata");
                return;
            }

            UUID milestoneId = UUID.fromString(milestoneIdStr);
            Milestones milestone = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new IllegalStateException("Milestone not found for ID: " + milestoneId));

            if (milestone.getStatus() == MilestoneStatus.milestoneStatus.PAID_OUT) {
                log.info("Milestone {} has already been paid out. Ignoring.", milestoneId);
            } else {
                milestone.setStatus(MilestoneStatus.milestoneStatus.PAID_OUT);
                milestoneRepository.save(milestone);
                log.info("Milestone {} successfully updated to PAID.", milestoneId);

                MilestoneFundedEvent milestoneFundedEvent = new MilestoneFundedEvent(this, milestoneId);

                paymentEmailListener.onMilestoneFunded(milestoneFundedEvent);
            }

            saveAuditLog(eventId, event.getType(), "PROCESSED", null);

        } catch (Exception e) {
            log.error("Fatal error processing webhook event {}: {}", eventId, e.getMessage());
            saveAuditLog(eventId, event.getType(), "FAILED", e.getMessage());

            throw e;
        }
    }

    public void handleCheckoutSessionCompleted(Event stripeEvent) {
        // Placeholder for future checkout flows
        // TODO: Implement checkout session completion logic
        log.info("Checkout session completed for event {}", stripeEvent.getId());
    }

    private void saveAuditLog(String stripeEventId, String type, String status, String errorMessage) {
        StripeWebhookLogs logEntry = StripeWebhookLogs.builder()
                .stripeEventId(stripeEventId)
                .eventType(type)
                .processingStatus(StripeWebhookProcessingStatus.valueOf(status))
                .errorMessage(errorMessage)
                .build();
        webhookLogsRepository.save(logEntry);
    }

}