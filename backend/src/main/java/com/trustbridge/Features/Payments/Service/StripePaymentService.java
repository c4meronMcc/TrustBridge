    package com.trustbridge.Features.Payments.Service;

    import com.stripe.model.*;
    import com.stripe.net.RequestOptions;
    import com.stripe.param.*;
    import com.trustbridge.Domain.Entities.*;
    import com.trustbridge.Domain.Enums.PaymentRequestStatus;
    import com.trustbridge.Domain.Repositories.*;
    import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.stereotype.Service;

    import java.math.BigDecimal;
    import java.util.Optional;

    @Service
    @Slf4j
    @RequiredArgsConstructor
    public class StripePaymentService {

        private final StripeAccountRepository stripeAccountRepository;
        private final StripeCustomerRepository stripeCustomerRepository;
        private final EmailServiceImpl emailServiceImpl;
        private final PaymentRequestRepository paymentRequestRepository;
        private final BankPaymentSessionRepository bankPaymentSessionRepository;
        private final UserRepository usersRepository;

        public void validatePaymentRequest(PaymentRequestStatus paymentRequestStatus) {
            switch (paymentRequestStatus) {
                case PAID -> throw new IllegalStateException("Payment request is already paid");
                case EXPIRED -> throw new IllegalStateException("Payment request has expired");
                case REFUNDED -> throw new IllegalStateException("Payment request has been refunded");
                case FAILED -> log.info("Payment request has failed previously, allowing retry");
                default -> log.info("Payment request is valid for checkout");
            }
        }

        public void savePaymentIntentSession(PaymentIntent paymentIntent, PaymentRequest paymentRequest) {
            BankPaymentSession session = bankPaymentSessionRepository.findByPaymentRequestId(paymentRequest.getId())
                    .orElse(new BankPaymentSession());

            session.setPaymentRequest(paymentRequest);
            session.setStripePiId(paymentIntent.getId());
            session.setStripeStatus(paymentIntent.getStatus());
            session.setRedirectUrl("PENDING");

            bankPaymentSessionRepository.save(session);
        }

        public String createSettlementIntent(PaymentRequest paymentRequest) {
            validatePaymentRequest(paymentRequest.getStatus());

            try {
                Long amountInPence = paymentRequest.getAmount().multiply(new BigDecimal("100")).longValue();
                String currency = paymentRequest.getMilestone().getJob().getCurrency().toLowerCase();

                PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                        .setAmount(amountInPence)
                        .setCurrency(currency)
                        .addPaymentMethodType("card")
                        .addPaymentMethodType("pay_by_bank")
                        .setTransferGroup(paymentRequest.getMilestone().getId().toString())
                        .putMetadata("payment_request_id", paymentRequest.getId().toString())
                        .putMetadata("milestone_id", paymentRequest.getMilestone().getId().toString())
                        .putMetadata("fallback_tracking", "enabled")
                        .putMetadata("fallback_trigger_source", "settlement_intent")
                        .build();

                RequestOptions requestOptions = RequestOptions.builder()
                        .setIdempotencyKey(paymentRequest.getId().toString())
                        .build();

                log.info("Sending request to Stripe for Amount: {} {}", amountInPence, currency);

                PaymentIntent intent = PaymentIntent.create(params, requestOptions);

                log.info("Stripe PaymentIntent generated successfully: {}", intent.getId());
                savePaymentIntentSession(intent, paymentRequest);

                return intent.getClientSecret();

            } catch (Exception e) {
                log.error("🚨 STRIPE API CRASHED: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to generate Stripe token", e);
            }
        }

        public String createFreelancerOnboardingLink(String freelancerEmail) {
            try {

                AccountCreateParams accountParams = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setCountry("GB")
                        .setEmail(freelancerEmail)
                        .build();

                Account account = Account.create(accountParams);

                Users freelancer = usersRepository.findByEmail(freelancerEmail)
                        .orElseThrow(() -> new RuntimeException("Freelancer not found"));

                StripeAccount stripeAccount = StripeAccount.builder()
                        .user(freelancer)
                        .stripeAccountId(account.getId())
                        .payoutsEnabled(false)
                        .build();

                stripeAccountRepository.save(stripeAccount);

                AccountLinkCreateParams linkParams = AccountLinkCreateParams.builder()
                        .setAccount(account.getId())
                        .setRefreshUrl("http://localhost:3000/dashboard/freelancer/onboarding/refresh")
                        .setReturnUrl("http://localhost:3000/dashboard/freelancer/onboarding/success")
                        .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                        .build();

                AccountLink accountLink = AccountLink.create(linkParams);
                return accountLink.getUrl();

            } catch (Exception e) {
                log.error("Failed to create stripe onboarding link", e);
                throw new RuntimeException("Failed to generate onboarding link", e);
            }
        }

        public void releaseFundsToFreelancer(String milestoneId, String freelancerStripeAccountId, long amountInPence, String paymentIntentId) {
            try {
                PaymentIntentRetrieveParams retrieveParams = PaymentIntentRetrieveParams.builder()
                        .addExpand("latest_charge.balance_transaction")
                        .build();
                PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, retrieveParams, null);

                long stripefee = paymentIntent.getLatestChargeObject().getBalanceTransactionObject().getFee();

                long trustBridgeCommission = (long) (amountInPence * 0.01);
                long payoutAmount = amountInPence - stripefee - trustBridgeCommission;

                TransferCreateParams transferParams = TransferCreateParams.builder()
                        .setAmount(payoutAmount)
                        .setCurrency("gbp")
                        .setDestination(freelancerStripeAccountId)
                        .setTransferGroup(milestoneId)
                        .build();

                Transfer transfer = Transfer.create(transferParams);
                log.info("Successfully released funds to freelancer: {}", transfer.getId());

                PayoutCreateParams payoutCreateParams = PayoutCreateParams.builder()
                        .setAmount(trustBridgeCommission)
                        .setCurrency("gbp")
                        .build();

                Payout payout = Payout.create(payoutCreateParams);
                log.info("Successfully released funds to TrustBridge: {}", payout.getId());

            } catch (Exception e) {
                log.error("Failed to release funds for Milestone {}: {}", milestoneId, e.getMessage());
                throw new RuntimeException("Failed to release funds", e);
            }
        }

        public String getOrCreateStripeCustomer(Users client) {
            try {
                Optional<StripeCustomer> existing = stripeCustomerRepository.findByUserId(client.getId());
                if (existing.isPresent()) {
                    return existing.get().getStripeCustomerId();
                }

                CustomerCreateParams params = CustomerCreateParams.builder()
                        .setName(client.getFirstName() + " " + client.getLastName())
                        .setEmail(client.getEmail())
                        .build();

                Customer stripeCustomer = Customer.create(params);

                StripeCustomer entity = StripeCustomer.builder()
                        .user(client)
                        .stripeCustomerId(stripeCustomer.getId())
                        .build();

                stripeCustomerRepository.save(entity);

                return stripeCustomer.getId();

            } catch (Exception e) {
                log.error("Failed to create Stripe Customer", e);
                throw new RuntimeException(e);
            }
        }
    }