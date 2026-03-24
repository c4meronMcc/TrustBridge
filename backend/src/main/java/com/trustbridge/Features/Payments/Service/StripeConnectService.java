package com.trustbridge.Features.Payments.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.AccountLink;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Transfer;
import com.stripe.param.*;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.StripeAccount;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.StripeAccountRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    public String CreateStripeCustomer(Users client) {

        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(client.getFirstName() + " " + client.getLastName())
                .setEmail(client.getEmail())
                .build();

        try {
            Customer stripeCustomer = Customer.create(params);
            return stripeCustomer.getId();
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe Customer: " + e.getMessage());
        }
    }

    public PaymentIntent createEscrowHold(Milestones milestone, Users client) {

        String stripeCustomerId = CreateStripeCustomer(client);

        Long amountInPence = milestone.getAmount().multiply(new BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPence)
                .setCurrency(milestone.getJob().getCurrency())
                .setCustomer(stripeCustomerId)
                .addPaymentMethodType("pay_by_bank")
                .putMetadata("milestone_id", milestone.getId().toString())
                .build();

        try {
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create PaymentIntent: " + e.getMessage());
        }
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