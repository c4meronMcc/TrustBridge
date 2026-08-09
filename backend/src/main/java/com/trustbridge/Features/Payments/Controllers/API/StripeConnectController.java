package com.trustbridge.Features.Payments.Controllers.API;

import com.trustbridge.Features.Payments.Service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/freelancer/stripe")
@RequiredArgsConstructor
public class StripeConnectController {

    private final StripePaymentService stripePaymentService;

    @PostMapping("/onboarding")
    public ResponseEntity<?> generateOnboardingLink(Authentication authentication) {
        try {
            String email = authentication.getName();

            log.info("Freelancer email: {}", email);

            String stripeUrl = stripePaymentService.createFreelancerOnboardingLink(email);

            return ResponseEntity.ok(Map.of("stripeUrl", stripeUrl));
        } catch (Exception e) {
            log.error("Error generating Stripe onboarding link", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate onboarding link"));
        }
    }
}
