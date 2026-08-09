package com.trustbridge.Features.Jobs.StateMachine.Interceptors;

import com.trustbridge.Domain.Entities.Dispute;
import com.trustbridge.Domain.Enums.DisputeEvent;
import com.trustbridge.Domain.Enums.DisputeState;
import com.trustbridge.Domain.Repositories.DisputeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class DisputeStateChangeInterceptor extends StateMachineInterceptorAdapter<DisputeState, DisputeEvent> {

    @Autowired
    DisputeRepository disputeRepository;

    /**
     * Handles state changes in the dispute state machine by updating the corresponding
     * dispute entity in the database. Ensures that the dispute's state is updated
     * according to the transition performed in the state machine.
     *
     * @param state the new state of the dispute after the transition
     * @param message the message triggering the event, containing additional headers and payload
     * @param transition the transition object representing the change in state
     * @param stateMachine the current state machine handling the state transition
     * @param rootStateMachine the root state machine associated with hierarchical state machines
     */
    @Override
    public void preStateChange(State<DisputeState, DisputeEvent> state,
                               Message<DisputeEvent> message,
                               Transition<DisputeState, DisputeEvent> transition,
                               StateMachine<DisputeState, DisputeEvent> stateMachine,
                               StateMachine<DisputeState, DisputeEvent> rootStateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {

            UUID disputeId = (UUID) msg.getHeaders().get("disputeId");

            if (disputeId != null) {
                Dispute dispute = disputeRepository.findById(disputeId)
                        .orElseThrow(() -> new RuntimeException("Dispute not found!"));

                String initialState = dispute.getState().name();

                dispute.setState(DisputeState.valueOf(state.getId().name()));

                disputeRepository.save(dispute);

                System.out.println("Dispute Status Changed From: " + initialState + " to: " + state.getId().name());
            }
        });
    }
}
