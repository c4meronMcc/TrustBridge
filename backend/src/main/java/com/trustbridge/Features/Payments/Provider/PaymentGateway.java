package com.trustbridge.Features.Payments.Provider;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Features.Payments.Provider.Dto.OnboardingResult;
import com.trustbridge.Features.Payments.Provider.Dto.PayoutResult;
import com.trustbridge.Features.Payments.Provider.Dto.SettlementResult;

public interface PaymentGateway {
    SettlementResult createSettlementIntent(PaymentRequest paymentRequest);
    OnboardingResult createProviderAccount(Users freelancer);
    PayoutResult releaseFunds(Milestones milestone, Users freelancer, PaymentRequest paymentRequest);
    String getOrCreateCustomerId(Users client);
    String getProviderName();
}
