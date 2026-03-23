package com.trustbridge.Features.Payments.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.AccountLink;
import com.stripe.param.AccountLinkCreateParams;
import com.trustbridge.Domain.Entities.StripeAccount;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.StripeAccountRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class StripeConnectService {

    StripeAccountRepository stripeAccountRepository;
    UserRepository userRepository;

    public StripeConnectService(StripeAccountRepository stripeAccountRepository) {
        this.stripeAccountRepository = stripeAccountRepository;
    }

    /**
     * TODO: Split this into two methods:
     * 1. createStripeAccount
     * 2. createStripeAccountLink
     */

    public String createStripeAccount(UUID userId) throws StripeException {

        Users freelancer = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Freelancer not found"));

        if (!stripeAccountRepository.existsById(userId)) {

            StripeAccount stripeAccount = new StripeAccount();

            stripeAccount.setUser(freelancer);
            stripeAccount.setStripeAccountId(UUID.randomUUID().toString());

            stripeAccountRepository.save(stripeAccount);

            AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                    .setAccount(stripeAccount.getStripeAccountId())
                    .setRefreshUrl("http://localhost:3000/onboarding/refresh")
                    .setReturnUrl("http://localhost:3000/onboarding/success")
                    .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                    .build();

            AccountLink accountLink = AccountLink.create(linkParams);

            return accountLink.getUrl();
        }

        return null;
    }
}
