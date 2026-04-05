package com.trustbridge.Features.Payments.Controllers.Webhooks;

import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.trustbridge.Features.Payments.Service.StripeWebhookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.stripe.model.Event.*;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String endPointSecret;

    private final StripeWebhookService stripeWebhookService;

    public StripeWebhookController(StripeWebhookService stripeWebhookService) {
        this.stripeWebhookService = stripeWebhookService;
    }

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, endPointSecret);
        } catch (Exception e) {
            System.out.println("Webhook Security Error: " + e.getMessage());
            return ResponseEntity.badRequest().body("Signature Verification Failed");
        }

        String rawJson = event.getDataObjectDeserializer().getRawJson();
        if (rawJson == null) {
            System.err.println("CRITICAL: Could not deserialize event: " + event.getType());
            return ResponseEntity.ok().body("Success");
        }

        switch (event.getType()) {
            case "checkout.session.completed" -> stripeWebhookService.handleCheckoutSessionCompleted(rawJson);
            case "payment_intent.succeeded" -> stripeWebhookService.handlePaymentIntentSucceeded(rawJson);
            default -> System.out.println("Unhandled event type: " + event.getType());
        }

        return ResponseEntity.ok().body("Success");
    }
}
