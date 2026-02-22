package com.trustbridge.Config;

import com.trustbridge.Domain.Enums.MilestoneStatus.*;
import com.trustbridge.Domain.Enums.MilestoneEvent.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import javax.swing.*;
import java.util.EnumSet;
import java.util.UUID;

@Configuration
@EnableStateMachine
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
                //Approved -> Paid Out (Funds Paid)
                .and()
                .withExternal()
                .source(milestoneStatus.APPROVED).target(milestoneStatus.PAID_OUT)
                .event(milestoneEvent.RELEASE_FUNDS)
                //Submitted -> Dispute  (Work Disputed)
                .and()
                .withExternal()
                .source(milestoneStatus.SUBMITTED).target(milestoneStatus.DISPUTE_NEGOTIATION)
                .event(milestoneEvent.RAISE_DISPUTE)
                //Dispute Negotiation -> Dispute Resolved (Dispute Resolved)
                .and()
                .withExternal()
                .source(milestoneStatus.DISPUTE_NEGOTIATION).target(milestoneStatus.DISPUTE_RESOLVED)
                .event(milestoneEvent.RESOLVE_DISPUTE)
                //Dispute Negotiation -> Dispute Arbitration (Dispute to arbitration)
                .and()
                .withExternal()
                .source(milestoneStatus.DISPUTE_NEGOTIATION).target(milestoneStatus.DISPUTE_ARBITRATION)
                .event(milestoneEvent.DISPUTE_TO_ARBITRATION);
    }

    @Bean
    public Action<milestoneStatus, milestoneEvent> updateMilestoneStatusAction() {
        return contex -> {
            UUID milestoneId = (UUID) contex.getMessageHeaders().get("milestoneId");
            System.out.println("Logic executed: Updating Milestone " + milestoneId + "to IN_PROGRESS");
        };
    }
}
