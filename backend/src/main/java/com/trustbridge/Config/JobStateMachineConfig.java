package com.trustbridge.Config;

import com.trustbridge.Domain.Enums.JobEvent.*;
import com.trustbridge.Domain.Enums.JobStatus.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;

import java.util.EnumSet;

@Configuration
@EnableStateMachine
public class JobStateMachineConfig extends EnumStateMachineConfigurerAdapter<jobStatus, jobEvent> {
    @Override
    public void configure(StateMachineStateConfigurer<jobStatus, jobEvent> states) throws Exception {
        states
                .withStates()
                .initial(jobStatus.DRAFT)
                .states(EnumSet.allOf(jobStatus.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<jobStatus, jobEvent> transitions) throws Exception {
        // UN-GUARDED STATE TRANSITIONS
        transitions
                // Draft -> Pending Acceptance (Publish Offer)
                .withExternal()
                .source(jobStatus.DRAFT).target(jobStatus.PENDING_ACCEPTANCE)
                .event(jobEvent.PUBLISH_OFFER)
                // Draft -> Cancelled unsure if this should actually exist
                .and()
                .withExternal()
                .source(jobStatus.DRAFT).target(jobStatus.CANCELLED)
                .event(jobEvent.CANCEL_JOB)
                // Pending Acceptance -> Cancelled (Client Cancels)
                .and()
                .withExternal()
                .source(jobStatus.PENDING_ACCEPTANCE).target(jobStatus.CANCELLED)
                .event(jobEvent.CANCEL_JOB)
                // Draft -> Submitted (Job Submission)
                .and()
                .withExternal()
                .source(jobStatus.SUBMITTED).target(jobStatus.IN_PROGRESS)
                .event(jobEvent.SUBMISSION_REVOKED);

        // GUARDED STATE TRANSITIONS
        transitions
                .withExternal()
                .source(jobStatus.PENDING_ACCEPTANCE).target(jobStatus.AWAITING_PAYMENT)
                .event(jobEvent.ACCEPT_OFFER)
                .and()
                .withExternal()
                .source(jobStatus.AWAITING_PAYMENT).target(jobStatus.IN_PROGRESS)
                .event(jobEvent.FUNDS_DEPOSITED)
                .and()
                .withExternal()
                .source(jobStatus.IN_PROGRESS).target(jobStatus.SUBMITTED)
                .event(jobEvent.ALL_MILESTONES_COMPLETED)
                .and()
                .withExternal()
                .source(jobStatus.SUBMITTED).target(jobStatus.APPROVED)
                .event(jobEvent.JOB_APPROVED)
                .and()
                .withExternal()
                .source(jobStatus.APPROVED).target(jobStatus.PAID_OUT)
                .event(jobEvent.RELEASE_PAYMENT)
                .and()
                .withExternal()
                .source(jobStatus.SUBMITTED).target(jobStatus.DISPUTED)
                .event(jobEvent.RAISE_DISPUTE)
                .and()
                .withExternal()
                .source(jobStatus.DISPUTED).target(jobStatus.IN_PROGRESS)
                .event(jobEvent.RESOLVE_DISPUTE);


    }

    @Bean
    public Guard<jobStatus, jobEvent> firstMilestonePaidGuard() {
        return context -> {
            // getting sequence order
            Integer sequence = context.getMessageHeaders().get("milestoneSequence", Integer.class);
            // getting isFunded header
            Boolean isFunded = context.getMessageHeaders().get("isFunded", Boolean.class);
            return sequence != null && sequence == 1
                    && isFunded != null && isFunded;
        };
    }
}
