package com.trustbridge.Features.Jobs.StateMachine.Interceptors;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;
import com.trustbridge.Domain.Enums.MilestoneStatus.*;
import com.trustbridge.Domain.Enums.MilestoneEvent.*;

import java.util.Optional;
import java.util.UUID;

@Component
public class MilestoneStateChangeInterceptor extends StateMachineInterceptorAdapter<milestoneStatus, milestoneEvent> {

    @Autowired
    MilestoneRepository milestoneRepository;

    /**
     * Handles pre-state change events in the milestone state machine by updating the associated
     * milestone entity in the database. This ensures the milestone's status is updated
     * based on the transition to the new state, as part of the state machine's execution lifecycle.
     *
     * @param state the new state to which the milestone transitions
     * @param message the message triggering the state transition, containing headers and payload information
     * @param transition the transition object representing the state change
     * @param stateMachine the state machine managing the milestone's state transitions
     * @param rootStateMachine the root state machine associated with the hierarchical structure of state machines
     */
    @Override
    public void preStateChange(State<milestoneStatus, milestoneEvent> state,
                               Message<milestoneEvent> message,
                               Transition<milestoneStatus, milestoneEvent> transition,
                               StateMachine<milestoneStatus, milestoneEvent> stateMachine,
                               StateMachine<milestoneStatus, milestoneEvent> rootStateMachine) {

        Optional.ofNullable(message).ifPresent(msg -> {

            UUID milestoneId = (UUID) msg.getHeaders().get("milestoneId");

            if (milestoneId != null) {
                Milestones milestone = milestoneRepository.findById(milestoneId)
                                .orElseThrow(() -> new RuntimeException("Milestone not found!"));

                String initialState = milestone.getStatus().name();

                milestone.setStatus(milestoneStatus.valueOf(state.getId().name()));

                milestoneRepository.save(milestone);

                System.out.println("Milestone Status Changed From: " + initialState + " to: " + state.getId().name());
            }
        });
    }
}
