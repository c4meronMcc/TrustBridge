package com.trustbridge.Features.Jobs.StateMachine;

import com.trustbridge.Domain.Enums.MilestoneStatus.milestoneStatus;
import com.trustbridge.Domain.Enums.MilestoneEvent.milestoneEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;
import java.util.UUID;

@Configuration
@EnableStateMachineFactory
public class MilestoneStateMachineConfig extends EnumStateMachineConfigurerAdapter<milestoneStatus, milestoneEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<milestoneStatus, milestoneEvent> states) throws Exception {
        states
            .withStates()
            .initial(milestoneStatus.LOCKED)
            .states(EnumSet.allOf(milestoneStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<milestoneStatus, milestoneEvent> transitions) throws Exception {
        // UN-GUARDED STATE TRANSITIONS
        transitions
                //Locked -> Awaiting Payment (Unlock)
                .withExternal()
                .source(milestoneStatus.LOCKED).target(milestoneStatus.AWAITING_PAYMENT)
                .event(milestoneEvent.UNLOCK)
                //In Progress -> Submitted (Submitted Work)
                .and()
                .withExternal()
                .source(milestoneStatus.IN_PROGRESS).target(milestoneStatus.SUBMITTED)
                .event(milestoneEvent.SUBMITTED_WORK)
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
                //Approved -> Paid Out (Funds Paid)
                .and()
                .withExternal()
                .source(milestoneStatus.APPROVED).target(milestoneStatus.PAID_OUT)
                .event(milestoneEvent.RELEASE_FUNDS)
                .guard(isClientApprovingGuard())
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

    @Bean
    public Action<milestoneStatus, milestoneEvent> updateMilestoneStatusAction() {
        return context -> {
            UUID milestoneId = (UUID) context.getMessageHeaders().get("milestoneId");
            System.out.println("Logic executed: Updating Milestone " + milestoneId + "to IN_PROGRESS");
        };
    }

    @Bean
    public Action<milestoneStatus, milestoneEvent> notifyFreelancerToStartAction() {
        return context -> {
            UUID milestoneId = context.getMessageHeaders().get("milestoneId", UUID.class);
            // In reality, you would inject an EmailService here and call it
            // TODO: Inject EmailService here!
            System.out.println("ACTION FIRED: Sending email to Freelancer for Milestone " + milestoneId + " - Funds secured, start working!");
        };
    }

    @Bean
    public Guard<milestoneStatus, milestoneEvent> isFundedGuard() {
        return context -> {
            Boolean isFunded = (Boolean) context.getMessageHeaders().get("isFunded");
            System.out.println("Guard executed: " + isFunded);
            return isFunded != null && isFunded;
        };
    }

    @Bean
    public Guard<milestoneStatus, milestoneEvent> isClientApprovingGuard() {
        return context -> {
            Boolean isClientApproving = (Boolean) context.getMessageHeaders().get("isClientApproving");
            System.out.println("Guard executed: " + isClientApproving);
            return isClientApproving != null && isClientApproving;
        };
    }

    @Bean
    public Guard<milestoneStatus, milestoneEvent> isPreviousMilestoneApprovedGuard() {
        return context -> {
            Boolean isPreviousMilestoneApproved = (Boolean) context.getMessageHeaders().get("isPreviousMilestoneApproved");
            System.out.println("Guard executed: " + isPreviousMilestoneApproved);
            return isPreviousMilestoneApproved != null && isPreviousMilestoneApproved;
        };
    }

    @Bean
    public Guard<milestoneStatus, milestoneEvent> onlyClientCanDisputeGuard() {
        return context -> {
            String userRole = context.getMessageHeaders().get("userRole", String.class);
            return "CLIENT".equals(userRole) || "ADMIN".equals(userRole);
        };
    }

    @Bean
    public Guard<milestoneStatus, milestoneEvent> mutualAgreementGuard() {
        return context -> {
            Boolean bothPartiesAgreed = context.getMessageHeaders().get("bothPartiesAgreed", Boolean.class);
            String userRole = context.getMessageHeaders().get("userRole", String.class);

            // Allowed if both agreed, OR if an admin forces the resolution
            return (bothPartiesAgreed != null && bothPartiesAgreed) || "ADMIN".equals(userRole);
        };
    }

    @Bean
    public Guard<milestoneStatus, milestoneEvent> escalationAllowedGuard() {
        return context -> {
            Integer daysInNegotiation = context.getMessageHeaders().get("daysInNegotiation", Integer.class);

            // Block escalation if it hasn't been at least 3 days
            return daysInNegotiation != null && daysInNegotiation >= 3;
        };
    }
}
