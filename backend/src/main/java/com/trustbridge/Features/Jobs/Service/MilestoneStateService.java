package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Jobs;
import com.trustbridge.Domain.Entities.Milestones;
import com.trustbridge.Domain.Repositories.JobRepository;
import com.trustbridge.Domain.Repositories.MilestoneRepository;
import com.trustbridge.Features.Jobs.StateMachine.Interceptors.MilestoneStateChangeInterceptor;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class MilestoneStateService {

    private final MilestoneRepository milestoneRepository;
    private final StateMachineFactory<milestoneStatus, milestoneEvent> stateMachineFactory;
    private final MilestoneStateChangeInterceptor milestoneInterceptor;
    private final JobRepository jobRepository;

    /**
     * Builds and initializes a state machine for a specific milestone, configured with
     * the appropriate status, events, and a state change interceptor.
     *
     * @param milestoneId The unique identifier of the milestone for which the state machine is being built.
     * @return A configured and initialized {@link StateMachine} instance representing the milestone's state and transitions.
     * @throws RuntimeException if the milestone with the specified ID is not found in the repository.
     */
    private StateMachine<milestoneStatus, milestoneEvent> buildStateMachine(UUID milestoneId) {

        Milestones milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found!"));

        StateMachine<milestoneStatus, milestoneEvent> sm = stateMachineFactory.getStateMachine(milestoneId.toString());

        sm.stopReactively().block();

        sm.getStateMachineAccessor().doWithAllRegions(accessor -> {

            accessor.addStateMachineInterceptor(milestoneInterceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    milestone.getStatus(), null, null, null
            )).block();
        });

        sm.startReactively().block();

        return sm;
    }

    /**
     * Activates the next locked milestone for a specified job by changing its state
     * to unlocked. If no locked milestones exist for the given job, no changes are made.
     *
     * @param jobId The unique identifier of the job for which the next locked milestone
     *              should be activated.
     */
    @Transactional
    public void activateNextLockedMilestoneForJob(UUID jobId) {
        Optional<Milestones> nextLockedMilestone = milestoneRepository
                .findFirstByJobIdAndStatusOrderBySequenceOrderAsc(jobId, milestoneStatus.LOCKED);

        if (nextLockedMilestone.isPresent()) {
            Milestones target = nextLockedMilestone.get();
            log.info("Found next step: Unlocking Milestone " + target.getId() + " (Sequence " + target.getSequenceOrder() + ")");

            this.milestoneActivated(target.getId());
        } else {
            log.info("All milestones for Job " + jobId + " are already unlocked or finished.");
        }
    }

    @Transactional
    public void moveMilestoneIntoSubmission(UUID milestoneId) {
        Milestones milestone = milestoneRepository.findById(milestoneId)
                .orElseThrow(() -> new RuntimeException("Milestone not found!"));

        try {
            this.workedSubmitted(milestoneId);
            log.info("Milestone {} moved into SUBMITTED_WORK state", milestone.getId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Fires an event for a specified milestone by leveraging the associated state machine.
     * The event is sent to the state machine to trigger a state transition for the milestone.
     *
     * @param milestoneId The unique identifier of the milestone for which the event should be fired.
     * @param event       The specific event to be fired, which may trigger a state transition in the state machine.
     */
    public void fireEvent(UUID milestoneId, milestoneEvent event) {
        StateMachine<milestoneStatus, milestoneEvent> sm = buildStateMachine(milestoneId);

        Message<milestoneEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("milestoneId", milestoneId)
                .build();

        sm.sendEvent(Mono.just(message)).blockLast();
    }

    /**
     * Activates a milestone by triggering the UNLOCK event through its associated state machine.
     * This changes the milestone's state from locked to unlocked, enabling further progress.
     *
     * @param milestoneId The unique identifier of the milestone to activate.
     */
    public void milestoneActivated(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.UNLOCK);
    }

    /**
     * Triggers the "Submitted Work" event for a specific milestone, indicating that
     * work has been submitted for review. This event is processed by the associated
     * state machine to handle the milestone's state transition.
     *
     * @param milestoneId The unique identifier of the milestone for which the "Submitted Work" event is triggered.
     */
    public void workedSubmitted(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.SUBMITTED_WORK);
    }

    /**
     * Revokes the submission for a specific milestone by triggering the "Revoke Submission"
     * event. This action is processed by the associated state machine, which handles the
     * state transition for the milestone*/
    public void revokeSubmission(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.REVOKE_SUBMISSION);
    }

    /**
     * Approves the work for a specific milestone by triggering the "Work Approved" event.
     * This event is processed by the associated state machine to handle the milestone's
     * state transition, indicating that the work has been accepted.
     *
     * @param milestoneId The unique identifier of the milestone for which the "Work Approved" event is triggered.
     */
    public void workApproved(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.WORK_APPROVED);
    }

    /**
     * Cancels a specific milestone by triggering the "Cancel Milestone" event.
     * This action is processed by the associated state machine, which handles
     * the state transition of the milestone to a canceled state.
     *
     * @param milestoneId The unique identifier of the milestone to be canceled.
     */
    public void cancelMilestone(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.CANCEL_MILESTONE);
    }

    /**
     * Triggers the "Funds Deposited" event for a specific milestone, indicating that
     * funds have been deposited for that milestone. The event is processed by the
     * associated state machine to handle the milestone's state transition.
     *
     * @param milestoneId The unique identifier of the milestone for which the "Funds Deposited" event is triggered.
     */
    public void fundsDeposited(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.FUNDS_DEPOSITED);
    }

    /**
     * Releases the funds associated with the specified milestone.
     *
     * @param milestoneId the unique identifier of the milestone for which funds
     *                    should be released
     */
    public void releaseFunds(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.RELEASE_FUNDS);
    }

    /**
     * Handles the event when a dispute is raised for a specific milestone.
     *
     * @param milestoneId the unique identifier of the milestone for which the dispute is raised
     */
    public void disputeRaised(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.RAISE_DISPUTE);
    }

    /**
     * Resolves a dispute for the specified milestone and triggers the corresponding event.
     *
     * @param milestoneId the unique identifier of the milestone for which the dispute is resolved
     */
    public void disputeResolved(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.RESOLVE_DISPUTE);
    }

    /**
     * Initiates the dispute resolution process for a specific milestone, escalating it to arbitration.
     *
     * @param milestoneId the unique identifier of the milestone being disputed
     */
    public void disputeToArbitration(UUID milestoneId) {
        fireEvent(milestoneId, milestoneEvent.DISPUTE_TO_ARBITRATION);
    }
}