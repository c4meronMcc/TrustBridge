package com.trustbridge.Features.Payments.Service;

import com.stripe.model.Account;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.checkout.Session;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.StripeAccount;
import com.trustbridge.Domain.Entities.StripeWebhookLogs;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Enums.StripeWebhookProcessingStatus;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.StripeAccountRepository;
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
    private final PaymentStateService paymentStateService;
    private final ApplicationEventPublisher eventPublisher;
    private final StripeAccountRepository stripeAccountRepository;
    private final MilestoneRepository milestoneRepository;

    @Transactional
    public void handlePaymentIntentSucceeded(Event event) {
        String eventId = event.getId();

        if (webhookLogsRepository.existsByStripeEventId(eventId)) {
            log.info("Idempotency trigger: Webhook event {} has already been processed. Ignoring.", eventId);
            return;
        }

        try {
            PaymentIntent paymentIntent = null;

            // 1. Try standard deserialization first
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
            }
            // 2. 💥 THE FIX: Fallback to raw JSON parsing for Open Banking payloads
            else if (event.getDataObjectDeserializer().getRawJson() != null) {
                paymentIntent = com.stripe.net.ApiResource.GSON.fromJson(
                        event.getDataObjectDeserializer().getRawJson(),
                        PaymentIntent.class
                );
            }

            if (paymentIntent == null) {
                throw new IllegalStateException("Deserialisation failed. Payload could not be parsed into a PaymentIntent.");
            }

            // 3. 💥 THE TYPO FIX: Ensure exact case matching with your builder
            String paymentRequestIdStr = paymentIntent.getMetadata().get("payment_request_id");
            String milestoneIdStr = paymentIntent.getMetadata().get("milestone_id");

            if (paymentRequestIdStr == null || milestoneIdStr == null) {
                log.warn("Webhook event {} has missing metadata. Request ID: {}, Milestone ID: {}",
                        eventId, paymentRequestIdStr, milestoneIdStr);
                saveAuditLog(eventId, "payment_intent.succeeded", StripeWebhookProcessingStatus.FAILED.name(), "Missing metadata");
                return;
            }

            UUID paymentRequestId = UUID.fromString(paymentRequestIdStr);
            UUID milestoneId = UUID.fromString(milestoneIdStr);

            paymentStateService.paymentSuccessful(paymentRequestId);

            MilestoneFundedEvent milestoneFundedEvent = new MilestoneFundedEvent(this, milestoneId);
            eventPublisher.publishEvent(milestoneFundedEvent);

            saveAuditLog(eventId, event.getType(), "PROCESSED", null); // Ensure "SUCCESS" matches your Enum!
            log.info("Webhook event {} processed successfully.", eventId);

        } catch (Exception e) {
            log.error("Fatal error processing webhook event {}: {}", eventId, e.getMessage());
            saveAuditLog(eventId, event.getType(), "FAILED", e.getMessage()); // Ensure "FAILED" matches your Enum!

            throw e;
        }
    }

    @Transactional
    public void handleAccountUpdated(Event event) {
        String eventId = event.getId();

        if (webhookLogsRepository.existsByStripeEventId(eventId)) {
            log.info("Idempotency trigger: Webhook event {} has already been processed. Ignoring.", eventId);
            return;
        }

        try {
            Account account = null;

            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                account = (Account) event.getDataObjectDeserializer().getObject().get();
            } else if (event.getDataObjectDeserializer().getRawJson() != null) {
                account = com.stripe.net.ApiResource.GSON.fromJson(
                        event.getDataObjectDeserializer().getRawJson(),
                        Account.class
                );
            }

            if (account == null) {
                throw new IllegalStateException("Deserialisation failed. Payload could not be parsed into an Account.");
            }

            String stripeAccountId = account.getId();

            boolean payoutsEnabled = Boolean.TRUE.equals(account.getPayoutsEnabled());

            log.info("Account updated for Stripe account {}. Payouts enabled: {}", stripeAccountId, payoutsEnabled);

            Optional<StripeAccount> stripeAccountOpt = stripeAccountRepository.findBystripeAccountId(stripeAccountId);

            if (stripeAccountOpt.isPresent()) {
                StripeAccount stripeAccount = stripeAccountOpt.get();
                stripeAccount.setPayoutsEnabled(payoutsEnabled);
                stripeAccountRepository.save(stripeAccount);

                log.info("Payouts enabled status updated for Stripe account {}.", stripeAccountId);
            } else {
                log.warn("Stripe account {} not found in the database.", stripeAccountId);
            }

            saveAuditLog(eventId, event.getType(), "PROCESSED", null);

        } catch (Exception e) {
            log.error("Fatal error processing webhook event {}: {}", eventId, e.getMessage());
            saveAuditLog(eventId, event.getType(), "FAILED", e.getMessage());
            throw e;
        }
    }

    /**
     * Handle the checkout.session.completed event
     * @param stripeEvent
     */
    public void handleCheckoutSessionCompleted(Event stripeEvent) {
        // TODO: Implement checkout session completion logic
        String eventId = stripeEvent.getId();

        // Check if the event has already been processed - if it has, skip processing
        if (webhookLogsRepository.existsByStripeEventId(eventId)) {
            log.info("Idempotency trigger: Webhook event {} has already been processed. Ignoring.", eventId);
            return;
        }

        try {
            Session session = null;

            if (stripeEvent.getDataObjectDeserializer().getObject().isPresent()) { // check that the object is present
                session = (Session) stripeEvent.getDataObjectDeserializer().getObject().get(); // break down the event into a Session object
            } else if (stripeEvent.getDataObjectDeserializer().getRawJson() != null) { // fallback to raw JSON parsing
                session = com.stripe.net.ApiResource.GSON.fromJson(
                        stripeEvent.getDataObjectDeserializer().getRawJson(),
                        Session.class
                );
            }

            // Check if the session is null
            if (session == null) {
                throw new IllegalStateException("Deserialisation failed. Payload could not be parsed into a Session.");
            }

            // extract the metadata from the session
            String paymentRequestIdStr = session.getMetadata() != null ? session.getMetadata().get("payment_request_id") : null;
            String milestoneIdStr = session.getMetadata() != null ? session.getMetadata().get("milestone_id") : null;

            // check if the metadata is present
            if (paymentRequestIdStr == null || milestoneIdStr == null) {
                log.warn("Missing metadata for event {}", eventId);
                saveAuditLog(eventId, stripeEvent.getType(), "FAILED", "Missing metadata");
                return;
            }

            // check if event status is "paid" and if so change the state of the payment request in the database
            if ("paid".equals(session.getPaymentStatus())) {
                log.info("Payment for session {} completed successfully.", session.getId());
                // TODO: Handle payment completion logic here (Change the state of the payment request)
            }
        } catch (Exception e) {
            log.error("Error processing checkout session completed event: {}", e.getMessage());
            saveAuditLog(eventId, stripeEvent.getType(), "FAILED", e.getMessage());
            throw e;
        }

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