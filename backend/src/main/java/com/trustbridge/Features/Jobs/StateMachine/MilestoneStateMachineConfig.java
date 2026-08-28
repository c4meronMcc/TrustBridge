package com.trustbridge.Features.Jobs.StateMachine;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Entities.PaymentRequest;
import com.trustbridge.Domain.Entities.Users;
import com.trustbridge.Domain.Enums.EmailTemplateType;
import com.trustbridge.Domain.Enums.MilestoneStatus;
import com.trustbridge.Domain.Enums.MilestoneStatus.milestoneStatus;
import com.trustbridge.Domain.Enums.MilestoneEvent.milestoneEvent;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Domain.Repositories.PaymentRequestRepository;
import com.trustbridge.Domain.Repositories.UserRepository;
import com.trustbridge.Features.Jobs.Service.JobStateService;
import com.trustbridge.Features.Notifications.Listeners.MilestoneEmailListener;
import com.trustbridge.Features.Notifications.Services.EmailServiceImpl;
import com.trustbridge.Features.Payments.Config.StripeConfig;
import com.trustbridge.Features.Payments.Events.MilestoneSubmittedForApprovalEvent;
import com.trustbridge.Features.Payments.Provider.Mock.MockPaymentGateway;
import com.trustbridge.Features.Payments.Provider.PaymentGateway;
import com.trustbridge.Features.Payments.Service.PaymentRequestService;
import com.trustbridge.Features.Payments.Service.StripeConnectService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.util.Assert;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Configuration
@EnableStateMachineFactory(name = "MilestoneStateMachineFactory")
@RequiredArgsConstructor
public class MilestoneStateMachineConfig extends EnumStateMachineConfigurerAdapter<milestoneStatus, milestoneEvent> {

    private final MilestoneRepository milestoneRepository;
    private final PaymentRequestService paymentRequestService;
    private final EmailServiceImpl emailService;
    private final UserRepository userRepository;
    private final MilestoneEmailListener milestoneEmailListener;
    private final StripeConnectService stripeConnectService;
    private final PaymentGateway paymentGateway;
    private final PaymentRequestRepository paymentRequestRepository;

    /**
     * Configures the state machine states for the MilestoneStateMachineConfig.
     *
     * The configuration initializes the state machine with the initial state set to
     * {@code milestoneStatus.LOCKED} and registers all possible states defined in the
     * {@code milestoneStatus} enumeration.
     *
     * @param states the {@link StateMachineStateConfigurer} used to configure the states
     *               of the state machine
     * @throws Exception if an error occurs during state machine configuration
     */
    @Override
    public void configure(StateMachineStateConfigurer<milestoneStatus, milestoneEvent> states) throws Exception {
        states
            .withStates()
            .initial(milestoneStatus.LOCKED)
            .states(EnumSet.allOf(milestoneStatus.class));
    }

    /**
     * Configures the state machine transitions for the MilestoneStateMachineConfig.
     *
     * This method defines both unguarded and guarded transitions between different
     * states of the milestone lifecycle, as defined in the {@code milestoneStatus}
     * and {@code milestoneEvent} enumerations. The transitions determine the state
     * changes and associated actions or guards in response to specific events.
     *
     * Unguarded state transitions:
     * - Defines transitions such as moving from LOCKED to AWAITING_PAYMENT on
     *   UNLOCK event, triggering actions like generating a payment request.
     * - Includes transitions like IN_PROGRESS to SUBMITTED on SUBMITTED_WORK
     *   event, and many others as outlined in the method logic.
     *
     * Guarded state transitions:
     * - Ensures conditions are met before a transition occurs, such as validating
     *   funds or client approval.
     * - Triggers actions like notifying a freelancer to start work upon transition.
     * - Includes various guards to ensure transitions are executed only in
     *   specific circumstances, such as mutual agreement or client dispute rights.
     *
     * @param transitions the {@link StateMachineTransitionConfigurer} used to configure
     *                    the state transitions of the state machine
     * @throws Exception if an error occurs during state machine transition configuration
     */
    @Override
    public void configure(StateMachineTransitionConfigurer<milestoneStatus, milestoneEvent> transitions) throws Exception {
        // UN-GUARDED STATE TRANSITIONS
        transitions
                //Locked -> Awaiting Payment (Unlock)
                .withExternal()
                .source(milestoneStatus.LOCKED).target(milestoneStatus.AWAITING_PAYMENT)
                .event(milestoneEvent.UNLOCK)
                .action(generatePaymentRequestAction())
                //In Progress -> Submitted (Submitted Work)
                .and()
                .withExternal()
                .source(milestoneStatus.IN_PROGRESS).target(milestoneStatus.SUBMITTED)
                .event(milestoneEvent.SUBMITTED_WORK)
                .action(notifyClientOfFreelancerMilestoneSubmissionAction())
                .action(notifyFreelancerThatClientApprovalEmailHasSentAction())
                //Submitted -> In Progress (Revoke Submission)
                .and()
                .withExternal()
                .source(milestoneStatus.SUBMITTED).target(milestoneStatus.IN_PROGRESS)
                .event(milestoneEvent.REVOKE_SUBMISSION)
                //Submitted -> Approved (Work Approved)
                .and()
                .withExternal()
                .source(milestoneStatus.SUBMITTED).target(milestoneStatus.APPROVED)
                .event(milestoneEvent.WORK_APPROVED)
                //Awaiting Payment -> Cancelled (Work Cancelled)
                .and()
                .withExternal()
                .source(milestoneStatus.AWAITING_PAYMENT).target(milestoneStatus.CANCELLED)
                .event(milestoneEvent.CANCEL_MILESTONE);

        // GUARDED STATE TRANSITIONS
        transitions
                //Awaiting Payment -> In Progress (Funds Deposited)
                .withExternal()
                .source(milestoneStatus.AWAITING_PAYMENT).target(milestoneStatus.IN_PROGRESS)
                .event(milestoneEvent.FUNDS_DEPOSITED)
                .guard(isFundedGuard())
                .action(notifyFreelancerToStartAction())
                //Approved -> Paid Out (Funds Paid)
                .and()
                .withExternal()
                .source(milestoneStatus.APPROVED).target(milestoneStatus.PAID_OUT)
                .event(milestoneEvent.RELEASE_FUNDS)
                .guard(isClientApprovingMilestoneGuard())
                .action(releaseEscrowFundsAction())
                //Submitted -> Dispute (Work Disputed)
                .and()
                .withExternal()
                .source(milestoneStatus.SUBMITTED).target(milestoneStatus.DISPUTE_NEGOTIATION)
                .event(milestoneEvent.RAISE_DISPUTE)
                .guard(onlyClientCanDisputeGuard())
                //Dispute Negotiation -> Dispute Resolved (Dispute Resolved)
                .and()
                .withExternal()
                .source(milestoneStatus.DISPUTE_NEGOTIATION).target(milestoneStatus.DISPUTE_RESOLVED)
                .event(milestoneEvent.RESOLVE_DISPUTE)
                .guard(mutualAgreementGuard())
                //Dispute Negotiation -> Dispute Arbitration (Dispute to arbitration)
                .and()
                .withExternal()
                .source(milestoneStatus.DISPUTE_NEGOTIATION).target(milestoneStatus.DISPUTE_ARBITRATION)
                .event(milestoneEvent.DISPUTE_TO_ARBITRATION)
                .guard(escalationAllowedGuard());
    }

    String guardExecuted = "Guard executed: ";
    String milestoneIdName = "milestoneId";

    /**
     * Executes the action to generate a payment request for a specific milestone.
     *
     * This action is triggered within the state machine flow and involves the following:
     * - Retrieves the milestone ID from the message headers.
     * - Validates the milestone ID to ensure it is not null.
     * - Fetches the milestone details from the repository.
     * - Invokes the payment request creation service for the specified milestone.
     *
     * Logs the successful creation of the payment request or throws a runtime exception
     * if the milestone is not found or an error occurs during the process.
     *
     * @return the action that performs payment request generation within the state machine.
     */
    @Bean Action<milestoneStatus, milestoneEvent> generatePaymentRequestAction() {
        return context -> {
            UUID milestoneId = context.getMessageHeaders().get( milestoneIdName , UUID.class);

            Assert .notNull(milestoneId, "Milestone ID is required!");
            Milestones milestones = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new RuntimeException("Milestone not found!"));

            try {
                paymentRequestService.createPaymentRequest(milestones);
                Logger.getLogger("ACTION FIRED: Payment Request created for Milestone " + milestoneId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    /**
     * Creates an action that notifies a freelancer to start working on a milestone.
     *
     * The method performs the following operations:
     * - Retrieves the milestone ID and freelancer ID from the message headers.
     * - Fetches the freelancer's details using the freelancer ID.
     * - Prepares an email body and sends an email notification to the freelancer,
     *   indicating that funds have been secured and they can begin work on the milestone.
     *
     * Additionally, logs a message indicating that the notification action was fired.
     *
     * @return an {@link Action} instance that sends an email notification to the freelancer
     *         to initiate work on the funded milestone.
     */
    @Bean
    public Action<milestoneStatus, milestoneEvent> notifyFreelancerToStartAction() {
        return context -> {
            UUID milestoneId = context.getMessageHeaders().get(milestoneIdName, UUID.class);
            Users freelancer = userRepository.getById(context.getMessageHeaders().get("freelancerId", UUID.class));
            String body = "This is a test email body. Please ignore. JOB ACCEPTED (CHANGE THIS TO HTML BODY)";



            emailService.sendEmail(
                    freelancer.getEmail(),
                    "x",
                    EmailTemplateType.MILESTONE_FUNDED_FREELANCER_NOTICE,
                    body,
                    milestoneId
            );
            Logger.getLogger("ACTION FIRED: Sending email to Freelancer for Milestone " + milestoneId + " - Funds secured, start working!");
        };
    }

    @Bean
    public Action<milestoneStatus, milestoneEvent> notifyClientOfFreelancerMilestoneSubmissionAction() {
        return context -> {
            UUID milestoneId = context.getMessageHeaders().get(milestoneIdName, UUID.class);

            MilestoneSubmittedForApprovalEvent event = new MilestoneSubmittedForApprovalEvent(this, milestoneId);

            milestoneEmailListener.handleFreelancerSubmitsMilestone(event);
            Logger.getLogger("ACTION FIRED: Sending email to Client for Milestone " + milestoneId + " - Freelancer submitted!");
        };
    }

    @Bean
    public Action<milestoneStatus, milestoneEvent> notifyFreelancerThatClientApprovalEmailHasSentAction() {
        return context -> {
            UUID milestoneId = context.getMessageHeaders().get(milestoneIdName, UUID.class);

            MilestoneSubmittedForApprovalEvent event = new MilestoneSubmittedForApprovalEvent(this, milestoneId);

            milestoneEmailListener.handleClientReceivesWaitingForApprovalEmail(event);
            Logger.getLogger("ACTION FIRED: Sending email to Freelancer for Milestone " + milestoneId + " - Client approval email sent!");
        };
    }

    @Bean
    public Action<milestoneStatus, milestoneEvent> releaseEscrowFundsAction() {
        return context -> {
            UUID milestoneId = context.getMessageHeaders().get(milestoneIdName, UUID.class);
            Milestones milestone = milestoneRepository.findById(milestoneId)
                    .orElseThrow(() -> new RuntimeException("Milestone not found!"));

            Users freelancer = milestone.getJob().getFreelancer();

            PaymentRequest paymentRequest = paymentRequestRepository.findByMilestone(milestone)
                    .orElseThrow(() -> new RuntimeException("Payment request not found!"));

            try {
                paymentGateway.releaseFunds(milestone, freelancer, paymentRequest);
                Logger.getLogger("ACTION FIRED: Escrow funds released for Milestone " + milestoneId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


        };
    }

    /**
     * Defines a guard condition that determines whether the "isFunded" condition is met
     * based on the message headers of the context.
     *
     * This guard is used in state transitions to ensure that the milestone's funding
     * status is validated before allowing certain transitions to proceed.
     * The condition checks if the "isFunded" flag exists in the message headers and if
     * its value is true.
     *
     * @return a {@link Guard} instance that evaluates the "isFunded" condition from
     *         the state machine message headers.
     */
    @Bean
    public Guard<milestoneStatus, milestoneEvent> isFundedGuard() {
        return context -> {
            Boolean isFunded = (Boolean) context.getMessageHeaders().get("isFunded");
            Logger.getLogger(getClass().getName()).info( guardExecuted + isFunded);
            return isFunded != null && isFunded;
        };
    }

    /**
     * Defines a guard condition that checks whether the client is approving the milestone.
     *
     * This guard evaluates the "isClientApproving" flag from the message headers within
     * the state machine context. If the flag exists and its value is true, the guard allows
     * the transition to proceed; otherwise, the transition is blocked.
     *
     * Logs the execution of the guard along with the value of the "isClientApproving" flag.
     *
     * @return a {@link Guard} instance that evaluates the "isClientApproving" condition from
     *         the state machine message headers.
     */
    @Bean
    public Guard<milestoneStatus, milestoneEvent> isClientApprovingMilestoneGuard() {
        return context -> {
            Boolean isClientApproving = (Boolean) context.getMessageHeaders().get("isClientApproving");
            Logger.getLogger(getClass().getName()).info( guardExecuted + isClientApproving);
            return isClientApproving != null && isClientApproving;
        };
    }

    /**
     * Defines a guard condition that determines whether the previous milestone has been approved.
     *
     * This guard evaluates the "isPreviousMilestoneApproved" flag from the message headers within
     * the state machine context. If the flag exists and its value is true, the guard allows
     * the transition to proceed. If the flag is absent or false, the transition is blocked.
     *
     * Logs the execution of the guard along with the value of the "isPreviousMilestoneApproved" flag.
     *
     * @return a {@link Guard} instance that evaluates the "isPreviousMilestoneApproved" condition
     *         from the state machine message headers.
     */
    @Bean
    public Guard<milestoneStatus, milestoneEvent> isPreviousMilestoneApprovedGuard() {
        return context -> {
            Boolean isPreviousMilestoneApproved = (Boolean) context.getMessageHeaders().get("isPreviousMilestoneApproved");
            Logger.getLogger(getClass().getName()).info(guardExecuted + isPreviousMilestoneApproved);
            return isPreviousMilestoneApproved != null && isPreviousMilestoneApproved;
        };
    }

    /**
     * Defines a guard condition that determines whether only a client or an admin can raise a dispute.
     *
     * This guard evaluates the "userRole" value from the message headers within the state machine context.
     * If the "userRole" equals "CLIENT" or "ADMIN", the guard allows the transition to proceed.
     * Otherwise, the transition is blocked.
     *
     * @return a {@link Guard} instance that validates if the user raising the dispute is either a client or an admin.
     */
    @Bean
    public Guard<milestoneStatus, milestoneEvent> onlyClientCanDisputeGuard() {
        return context -> {
            String userRole = context.getMessageHeaders().get("userRole", String.class);
            return "CLIENT".equals(userRole) || "ADMIN".equals(userRole);
        };
    }

    /**
     * Defines a guard condition that verifies whether a transition can occur based on
     * mutual agreement between parties or administrative privileges.
     *
     * This guard evaluates the "bothPartiesAgreed" and "userRole" values from the
     * message headers within the state machine context. The conditions for allowing
     * the transition are as follows:
     * - If "bothPartiesAgreed" exists and is true, the transition is permitted.
     * - If "userRole" equals "ADMIN", the transition is also permitted.
     *
     * This guard ensures flexibility for transitions by allowing consensual agreements
     * or administrative overrides.
     *
     * @return a {@link Guard} instance that determines whether the mutual agreement
     *         or administrative force conditions are satisfied.
     */
    @Bean
    public Guard<milestoneStatus, milestoneEvent> mutualAgreementGuard() {
        return context -> {
            Boolean bothPartiesAgreed = context.getMessageHeaders().get("bothPartiesAgreed", Boolean.class);
            String userRole = context.getMessageHeaders().get("userRole", String.class);

            // Allowed if both agreed, OR if an admin forces the resolution
            return (bothPartiesAgreed != null && bothPartiesAgreed) || "ADMIN".equals(userRole);
        };
    }

    /**
     * Creates and returns a guard that determines whether an escalation is allowed
     * based on the number of days in negotiation.
     *
     * The guard checks the "daysInNegotiation" header from the message context.
     * If the number of days is at least 3, escalation is allowed. Otherwise, it is blocked.
     *
     * @return a guard that evaluates if the escalation criteria are met
     */
    @Bean
    public Guard<milestoneStatus, milestoneEvent> escalationAllowedGuard() {
        return context -> {
            Integer daysInNegotiation = context.getMessageHeaders().get("daysInNegotiation", Integer.class);

            return daysInNegotiation != null && daysInNegotiation >= 3;
        };
    }
}
