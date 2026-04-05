package com.trustbridge.Features.Payments.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.StripeCustomer;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Repositories.StripeCustomerRepository;
import com.trustbridge.Features.Jobs.Service.EmailService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class StripePaymentService {

    private final StripeCustomerRepository stripeCustomerRepository;
    private final EmailService emailService;

    public StripePaymentService(StripeCustomerRepository stripeCustomerRepository, EmailService emailService) {
        this.stripeCustomerRepository = stripeCustomerRepository;
        this.emailService = emailService;
    }

    public PaymentIntent createCardPaymentIntent(Milestones milestone, Users client) throws StripeException {

        String stripeCustomerId = getOrCreateStripeCustomer(client);

        long amountInPence = milestone.getAmount().multiply(new java.math.BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPence)
                .setCurrency(milestone.getJob().getCurrency())
                .setCustomer(stripeCustomerId)
                .addPaymentMethodType("card")
                .putMetadata("milestone_id", milestone.getId().toString())
                .build();

        return PaymentIntent.create(params);
    }

    public PaymentIntent createVibansPaymentIntent(Milestones milestone, Users client) throws StripeException {
        String stripeCustomerId = getOrCreateStripeCustomer(client);

        long amountInPence = milestone.getAmount().multiply(new java.math.BigDecimal(100)).longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPence)
                .setCurrency(milestone.getJob().getCurrency())
                .setCustomer(stripeCustomerId)
                .addPaymentMethodType("customer_balance")
                .setConfirm(true)
                .setPaymentMethodData(
                        PaymentIntentCreateParams.PaymentMethodData.builder()
                                .setType(PaymentIntentCreateParams.PaymentMethodData.Type.CUSTOMER_BALANCE)
                                .build()
                )
                .setPaymentMethodOptions(
                        PaymentIntentCreateParams.PaymentMethodOptions.builder()
                                .setCustomerBalance(
                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.builder()
                                                .setFundingType(
                                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.FundingType.BANK_TRANSFER
                                                )
                                                .setBankTransfer(
                                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.BankTransfer.builder()
                                                                .setType(
                                                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.BankTransfer.Type.GB_BANK_TRANSFER
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("milestone_id", milestone.getId().toString())
                .build();
        return PaymentIntent.create(params);
    }

    public String getOrCreateStripeCustomer(Users client) throws StripeException {
        // Return existing customer ID if already created
        Optional<StripeCustomer> existing = stripeCustomerRepository.findByUserId(client.getId());
        if (existing.isPresent()) {
            return existing.get().getStripeCustomerId();
        }

        // Create new Stripe Customer
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setName(client.getFirstName() + " " + client.getLastName())
                .setEmail(client.getEmail())
                .build();

        Customer stripeCustomer = Customer.create(params);

        // Save to DB
        StripeCustomer entity = StripeCustomer.builder()
                .user(client)
                .stripeCustomerId(stripeCustomer.getId())
                .build();
        stripeCustomerRepository.save(entity);

        return stripeCustomer.getId();
    }

    public PaymentIntent createEscrowHold(Milestones milestone, Users client) throws StripeException {

        String stripeCustomerId = getOrCreateStripeCustomer(client);

        Long amountInPence = milestone.getAmount()
                .multiply(new BigDecimal(100))
                .longValue();

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInPence)
                .setCurrency("gbp")
                .setCustomer(stripeCustomerId)
                .addPaymentMethodType("customer_balance")
                .setPaymentMethodOptions(
                        PaymentIntentCreateParams.PaymentMethodOptions.builder()
                                .setCustomerBalance(
                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.builder()
                                                .setFundingType(
                                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.FundingType.BANK_TRANSFER
                                                )
                                                .setBankTransfer(
                                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.BankTransfer.builder()
                                                                .setType(
                                                                        PaymentIntentCreateParams.PaymentMethodOptions.CustomerBalance.BankTransfer.Type.GB_BANK_TRANSFER
                                                                )
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .putMetadata("milestone_id", milestone.getId().toString())
                .build();

        return PaymentIntent.create(params);
    }

    public void sendVibanEmail(Users client, PaymentIntent paymentIntent, Milestones milestone) {
        // Extract the VIBAN details Stripe returns
        var bankTransfer = paymentIntent.getNextAction().getDisplayBankTransferInstructions();
        var financialAddress = bankTransfer.getFinancialAddresses().get(0);

        String sortCode = financialAddress.getSortCode().getSortCode();
        String accountNumber = financialAddress.getSortCode().getAccountNumber();
        String reference = bankTransfer.getReference();

        emailService.sendBankTransferDetails(
                client.getEmail(),
                milestone.getTitle(),
                sortCode,
                accountNumber,
                reference,
                milestone.getAmount()
        );
    }

}
