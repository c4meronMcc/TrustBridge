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

    /**
     * Configures the states of the state machine.
     *
     * @param states the {@link StateMachineStateConfigurer} to define the states
     *               and their relationships in the state machine.
     * @throws Exception if an error occurs during configuration.
     */
    @Override
    public void configure(StateMachineStateConfigurer<DisputeState, DisputeEvent> states ) throws Exception {
        states
                .withStates()
                .initial(DisputeState.AWAITING_EVIDENCE)
                .states(EnumSet.allOf(DisputeState.class));
    }

    /**
     * Configures the state transitions for the dispute state machine. Defines the transitions
     * between various {@link DisputeState} based on the occurrence of specific {@link DisputeEvent}.
     *
     * @param transitions the {@link StateMachineTransitionConfigurer} used to configure
     *                    transition rules and associated actions in the state machine.
     * @throws Exception if an error occurs during the configuration of transitions.
     */
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

    /**
     * Processes the proposal action as part of the state machine workflow handling disputes.
     * This method increments the negotiation round count stored in the state machine's extended
     * state and determines the next action based on the negotiation state.
     * If the maximum number of negotiation rounds (3) is reached, this method triggers an
     * escalation event to move the state machine into arbitration. Otherwise, it continues
     * the negotiation process by calculating revised figures for the dispute.
     *
     * @return An {@link Action} that represents the logic for processing a proposal in the dispute
     *         state machine, including incrementing the round count and triggering state transitions
     *         based on negotiation progression.
     */
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
