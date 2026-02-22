package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import com.trustbridge.Domain.Enums.MilestoneStatus.*;
import com.trustbridge.Domain.Enums.MilestoneEvent.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class MilestoneStateService {

    MilestoneRepository milestoneRepository;

    @Autowired
    StateMachineFactory<milestoneStatus, milestoneEvent> stateMachineFactory;

    public MilestoneStateService(MilestoneRepository milestoneRepository) {
        this.milestoneRepository = milestoneRepository;
    }

    private StateMachine<milestoneStatus, milestoneEvent> buildStateMachine(UUID milestoneId) {

        Milestones milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found!"));

        StateMachine<milestoneStatus, milestoneEvent> sm = stateMachineFactory.getStateMachine(milestoneId.toString());

        sm.stopReactively();

        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {
            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    milestone.getStatus(),
                    null,
                    null,
                    null
            ));
        });

        sm.startReactively();

        return sm;

    }

    public void fireEvent(UUID milestoneId, milestoneEvent event) {
        StateMachine<milestoneStatus, milestoneEvent> sm = buildStateMachine(milestoneId);

        Message<milestoneEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("milestoneId", milestoneId)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
    }

    // UN-GUARDED STATE TRANSITIONS
    public void milestoneActivated(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.UNLOCK);
    }

    public void workedSubmitted(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.SUBMITTED_WORK);
    }

    public void revokeSubmission(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.REVOKE_SUBMISSION);
    }

    public void workApproved(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.WORK_APPROVED);
    }

    public void cancelMilestone(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.CANCEL_MILESTONE);
    }

    // GUARDED STATE TRANSITIONS
    public void fundsDeposited(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.FUNDS_DEPOSITED);
    }

    public void releaseFunds(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.RELEASE_FUNDS);
    }

    public void disputeRaised(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.RAISE_DISPUTE);
    }

    public void disputeResolved(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.RESOLVE_DISPUTE);
    }

    public void disputeToArbitration(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.DISPUTE_TO_ARBITRATION);
    }
}