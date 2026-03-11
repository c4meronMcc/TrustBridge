package com.trustbridge.Features.Jobs.StateMachine;

import com.trustbridge.Domain.Enums.DisputeEvent;
import com.trustbridge.Domain.Enums.DisputeState;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachineFactory;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

import java.util.EnumSet;

@Configuration
@EnableStateMachineFactory(name = "DisputeStateMachineFactory")
public class DisputeStateMachineConfig extends EnumStateMachineConfigurerAdapter<DisputeState, DisputeEvent> {

    @Override
    public void configure(StateMachineStateConfigurer<DisputeState, DisputeEvent> states ) throws Exception {
        states
                .withStates()
                .initial(DisputeState.AWAITING_EVIDENCE)
                .states(EnumSet.allOf(DisputeState.class));
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<DisputeState, DisputeEvent> transitions) throws Exception {
        transitions
                .withExternal()
                .source(DisputeState.AWAITING_EVIDENCE).target(DisputeState.AWAITING_PROPOSAL)
                .event(DisputeEvent.SUBMIT_EVIDENCE)
                .and()
                .withExternal()
                .source(DisputeState.AWAITING_PROPOSAL).target(DisputeState.AWAITING_DISPUTE_DECISION)
                .event(DisputeEvent.SUBMIT_PROPOSAL)
                .action(processProposalAction())
                .and()
                .withExternal()
                .source(DisputeState.AWAITING_DISPUTE_DECISION).target(DisputeState.RESOLVED_AGREEMENT)
                .event(DisputeEvent.ACCEPT_PROPOSAL)
                .and()
                .withExternal()
                .source(DisputeState.AWAITING_DISPUTE_DECISION).target(DisputeState.RESOLVED_AGREEMENT)
                .event(DisputeEvent.ACCEPT_MIDDLE)
                .and()
                .withExternal()
                .source(DisputeState.AWAITING_DISPUTE_DECISION).target(DisputeState.AWAITING_PROPOSAL)
                .event(DisputeEvent.REJECT_PROPOSAL)
                .and()
                .withExternal()
                .source(DisputeState.AWAITING_PROPOSAL).target(DisputeState.AWAITING_EXTRA_EVIDENCE)
                .event(DisputeEvent.ESCALATE)
                .and()
                .withExternal()
                .source(DisputeState.AWAITING_EXTRA_EVIDENCE).target(DisputeState.UNDER_ARBITRATION)
                .event(DisputeEvent.SUBMIT_EVIDENCE)
                .and()
                .withExternal()
                .source(DisputeState.UNDER_ARBITRATION).target(DisputeState.RESOLVED_ARBITRATED)
                .event(DisputeEvent.ADMIN_DECIDED);
    }

    @Bean
    public Action<DisputeState, DisputeEvent> processProposalAction() {
        return context -> {
            // 1. Get the current round from the machine's memory (default to 0 if null)
            Integer currentRound = context.getExtendedState().get("negotiationRound", Integer.class);
            if (currentRound == null) currentRound = 0;

            // 2. Add 1 for this new proposal
            currentRound++;

            // 3. Save it back to memory
            context.getExtendedState().getVariables().put("negotiationRound", currentRound);

            System.out.println("Processing Proposal... This is round " + currentRound);

            // 4. THE MAGIC CHECK
            if (currentRound >= 3) {
                System.out.println("Maximum negotiation rounds reached! Escalating to Arbitration.");
                // Fire the ESCALATE event automatically!
                context.getStateMachine().sendEvent(
                        MessageBuilder.withPayload(DisputeEvent.ESCALATE).build()
                );
            } else {
                // If under 3 rounds, we just let them keep negotiating
                System.out.println("Calculating new 'Meet in the Middle' figures...");
                // (We will handle the actual database math in the DisputeService later)
            }
        };
    }

}
