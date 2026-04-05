package com.trustbridge.Features.Payments.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.Transfer;
import com.stripe.param.*;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.StripeAccount;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.StripeAccountRepository;
import com.trustbridge.Domain.Repositories.StripeCustomerRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class StripeConnectService {

    private final StripeAccountRepository stripeAccountRepository;
    private final StripeCustomerRepository stripeCustomerRepository;
    private final UserRepository userRepository;

    public StripeConnectService(
            StripeAccountRepository stripeAccountRepository,
            StripeCustomerRepository stripeCustomerRepository,
            UserRepository userRepository) {
        this.stripeAccountRepository = stripeAccountRepository;
        this.stripeCustomerRepository = stripeCustomerRepository;
        this.userRepository = userRepository;
    }

    public String createStripeAccount(Users freelancer) throws StripeException {

        AccountCreateParams accountParams = AccountCreateParams.builder()
                .setType(AccountCreateParams.Type.EXPRESS)
                .setEmail(freelancer.getEmail())
                .build();
        Account account = Account.create(accountParams);

        StripeAccount stripeAccount = StripeAccount.builder()
                .user(freelancer)
                .stripeAccountId(account.getId())
                .payoutsEnabled(false)
                .build();

        stripeAccountRepository.save(stripeAccount);

        AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                .setAccount(account.getId())
                .setRefreshUrl("http://localhost:3000/onboarding/refresh")
                .setReturnUrl("http://localhost:3000/onboarding/success")
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(linkParams);
        return accountLink.getUrl();
    }

    public Transfer releaseEscrowFunds(Milestones milestone, Users freelancer) {

        StripeAccount freelancerAccount = stripeAccountRepository.findByUserId(freelancer.getId())
                .orElseThrow(() -> new RuntimeException("Freelancer account not found"));

        String freelancerAccountId = freelancerAccount.getStripeAccountId();

        long amountInPence = milestone.getAmount().multiply(new BigDecimal(100)).longValue();
        long platformFeeInPence = milestone.getAmount().multiply(new BigDecimal(0.02)).longValue();
        long amountToFreelancer = amountInPence - platformFeeInPence;

        TransferCreateParams params = TransferCreateParams.builder()
                .setAmount(amountToFreelancer)
                .setCurrency(milestone.getJob().getCurrency())
                .setDestination(freelancerAccountId)
                .setTransferGroup(milestone.getId().toString())
                .build();

        try {
            Transfer transfer = Transfer.create(params);
            System.out.println("Successfully released funds to freelancer: " + freelancer.getEmail());
            return transfer;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to release funds: " + e.getMessage());
        }
    }
}