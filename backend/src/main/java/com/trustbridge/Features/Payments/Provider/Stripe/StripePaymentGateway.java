package com.trustbridge.Features.Payments.Provider.Stripe;

import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Entities.StripeAccount;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.StripeAccountRepository;
import com.trustbridge.Features.Payments.Provider.Dto.OnboardingResult;
import com.trustbridge.Features.Payments.Provider.Dto.PayoutResult;
import com.trustbridge.Features.Payments.Provider.Dto.SettlementResult;
import com.trustbridge.Features.Payments.Provider.PaymentGateway;
import com.trustbridge.Features.Payments.Service.StripeConnectService;
import com.trustbridge.Features.Payments.Service.StripePaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Profile("!mock")
@RequiredArgsConstructor
public class StripePaymentGateway implements PaymentGateway {

    private final StripePaymentService stripePaymentService;
    private final StripeConnectService stripeConnectService;
    private final StripeAccountRepository stripeAccountRepository;

    @Override
    public SettlementResult createSettlementIntent(PaymentRequest paymentRequest) {
        String clientSecret = stripePaymentService.createSettlementIntent(paymentRequest);
        // StripePaymentService doesn't return the ID easily in createSettlementIntent, 
        // but it's saved in the bankPaymentSession. 
        // For SettlementResult, we might need to adjust StripePaymentService or just return what we have.
        return new SettlementResult(clientSecret, null, null);
    }

    @Override
    public OnboardingResult createProviderAccount(Users freelancer) {
        String url = stripePaymentService.createFreelancerOnboardingLink(freelancer.getEmail());
        return new OnboardingResult(null, url);
    }

    @Override
    public PayoutResult releaseFunds(Milestones milestone, Users freelancer, PaymentRequest paymentRequest) {
        Transfer transfer = stripeConnectService.releaseEscrowFunds(milestone, freelancer);

        return new PayoutResult(transfer.getId(), transfer.getAmount());
    }

    @Override
    public String getOrCreateCustomerId(Users client) {
        return stripePaymentService.getOrCreateStripeCustomer(client);
    }

    @Override
    public String getProviderName() {
        return "stripe";
    }
}
