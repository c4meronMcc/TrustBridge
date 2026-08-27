package com.trustbridge.Features.Payments.Controllers.API;

import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Payments.Provider.Dto.OnboardingResult;
import com.trustbridge.Features.Payments.Provider.Mock.MockPaymentGateway;
import com.trustbridge.Features.Payments.Provider.PaymentGateway;
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

    private final PaymentGateway paymentGateway;
    private final UserRepository userRepository;

    @PostMapping("/onboarding")
    public ResponseEntity<?> generateOnboardingLink(Authentication authentication) {
        try {
            String email = authentication.getName();
            Users freelancer = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Freelancer not found"));

            log.info("Freelancer email: {}", email);
            String provider = paymentGateway.getProviderName();

            if ("mock".equals(provider)) {
                return ResponseEntity.ok(Map.of(
                        "provider", provider,
                        "stripeUrl", "",
                        "verified", true
                ));
            }

            OnboardingResult result = paymentGateway.createProviderAccount(freelancer);

            return ResponseEntity.ok(Map.of(
                    "provider", provider,
                    "stripeUrl", result.onboardingUrl() != null ? result.onboardingUrl() : "",
                    "verified", false
            ));
        } catch (Exception e) {
            log.error("Error generating onboarding link", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to generate onboarding link"));
        }
    }
}
