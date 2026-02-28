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
