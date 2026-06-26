package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.StateMachine.Interceptors.MilestoneStateChangeInterceptor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import com.trustbridge.Domain.Enums.MilestoneStatus.*;
import com.trustbridge.Domain.Enums.MilestoneEvent.*;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MilestoneStateService {

    private final MilestoneRepository milestoneRepository;
    private final StateMachineFactory<milestoneStatus, milestoneEvent> stateMachineFactory;
    private final MilestoneStateChangeInterceptor milestoneInterceptor;
    private final JobRepository jobRepository;



    private StateMachine<milestoneStatus, milestoneEvent> buildStateMachine(UUID milestoneId) {

        // get the milestone
        Milestones milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found!"));

        // create the state machine
        StateMachine<milestoneStatus, milestoneEvent> sm = stateMachineFactory.getStateMachine(milestoneId.toString());

        // reset the state machine
        sm.stopReactively().block();

        // add the state change interceptor
        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {

            accessor.addStateMachineInterceptor(milestoneInterceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    milestone.getStatus(), null, null, null
            )).block();
        });

        // start the state machine
        sm.startReactively().block();

        return sm;
    }

    @Transactional
    public void activateNextLockedMilestoneForJob(UUID jobId) {
        Optional<Milestones> nextLockedMilestone = milestoneRepository
                .findFirstByJobIdAndStatusOrderBySequenceOrderAsc(jobId, milestoneStatus.LOCKED);

        if (nextLockedMilestone.isPresent()) {
            Milestones target = nextLockedMilestone.get();
            System.out.println("Found next step: Unlocking Milestone " + target.getId() + " (Sequence " + target.getSequenceOrder() + ")");

            this.milestoneActivated(target.getId());
        } else {
            System.out.println("All milestones for Job " + jobId + " are already unlocked or finished.");
        }
    }

    public void fireEvent(UUID milestoneId, milestoneEvent event) {
        StateMachine<milestoneStatus, milestoneEvent> sm = buildStateMachine(milestoneId);

        Message<milestoneEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("milestoneId", milestoneId)
                .build();

        sm.sendEvent(Mono.just(message)).blockLast();
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