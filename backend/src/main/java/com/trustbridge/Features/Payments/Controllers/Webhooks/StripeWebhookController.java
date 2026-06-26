package com.trustbridge.Features.Payments.Controllers.Webhooks;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.trustbridge.Features.Payments.Service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.Event.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endPointSecret;

    private final StripeWebhookService stripeWebhookService;


    @PostMapping("/stripe")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endPointSecret);
        } catch (SignatureVerificationException e) {
            log.error("Webhook Security Error: Invalid Signature - {}", e.getMessage());
            return ResponseEntity.badRequest().body("Signature Verification Failed");
        } catch (Exception e) {
            log.error("Webhook Parsing Error: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Webhook Parsing Failed");
        }

        log.info("Received Stripe webhook Event: {} (ID: {})", event.getType(), event.getId());

        try {
            switch (event.getType()) {
                case "checkout.session.completed" -> stripeWebhookService.handleCheckoutSessionCompleted(event);
                case "payment_intent.succeeded" -> stripeWebhookService.handlePaymentIntentSucceeded(event);
                case "account.updated" -> stripeWebhookService.handleAccountUpdated(event);
                default -> System.out.println("Unhandled event type: " + event.getType());
            }
        } catch (Exception e) {
                log.error("Error processing Stripe webhook Event: {}: {}", event.getId(), e.getMessage());
        }

        return ResponseEntity.ok("Success");
    }
}