package com.trustbridge.Features.Jobs.Service;

import com.trustbridge.Domain.Entities.Dispute;
import com.trustbridge.Domain.Enums.DisputeEvent;
import com.trustbridge.Domain.Enums.DisputeState;
import com.trustbridge.Domain.Repositories.DisputeRepository;
import com.trustbridge.Features.Jobs.StateMachine.Interceptors.DisputeStateChangeInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DisputeStateService {

    DisputeRepository disputeRepository;

    @Autowired
    StateMachineFactory<DisputeState, DisputeEvent> stateMachineFactory;

    @Autowired
    DisputeStateChangeInterceptor disputeInterceptor;

    /**
     * Builds and initializes a state machine for the given dispute.
     *
     * The method fetches the current dispute from the repository, creates a state machine
     * associated with the dispute ID, configures it with necessary interceptors, resets its
     * state to the current dispute state, and starts it.
     *
     * @param disputeId the unique identifier of the dispute for which the state machine is to be created
     * @return the initialized state machine for the given dispute
     */
    private StateMachine<DisputeState, DisputeEvent> buildStateMachine(UUID disputeId) {

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Dispute not found!"));

        StateMachine<DisputeState, DisputeEvent> sm = stateMachineFactory.getStateMachine(disputeId.toString());

        sm.stopReactively().block();

        sm.getStateMachineAccessor().doWithAllRegions( accessor -> {

            accessor.addStateMachineInterceptor(disputeInterceptor);

            accessor.resetStateMachineReactively(new DefaultStateMachineContext<>(
                    dispute.getState(), null, null, null
            )).block();
        });
        sm.startReactively().block();

        return sm;
    }

    /**
     * Triggers a state transition event for a specified dispute.
     *
     * This method constructs a state machine associated with the provided dispute ID,
     * builds a message with the specified event and the dispute ID in its header,
     * and sends the event to the state machine for processing.
     *
     * @param disputeId the unique identifier of the dispute for which the event is to be fired
     * @param event the event representing the state transition to be triggered
     */
    public void fireEvent(UUID disputeId, DisputeEvent event) {
        StateMachine<DisputeState, DisputeEvent> sm = buildStateMachine(disputeId);

        Message<DisputeEvent> message = MessageBuilder
                .withPayload(event)
                .setHeader("disputeId", disputeId)
                .build();

        sm.sendEvent(Mono.just(message)).subscribe();
    }

    /**
     * Initiates a dispute by triggering the state transition event associated with opening a dispute.
     *
     * This method sends the {@code OPEN_DISPUTE} event to the state machine of the dispute identified
     * by the specified dispute ID, thereby setting the dispute into an open state.
     *
     * @param disputeId the unique identifier of the dispute to be opened
     */
    public void disputeRaised(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.OPEN_DISPUTE);
    }

    /**
     * Submits evidence for a specified dispute by triggering the state transition event
     * associated with evidence submission.
     *
     * This method sends the {@code SUBMIT_EVIDENCE} event to the state machine of the dispute
     * identified by the provided dispute ID, indicating that evidence has been submitted
     * for the dispute.
     *
     * @param disputeId the unique identifier of the dispute for which evidence is being submitted
     */
    public void disputeEvidenceSubmitted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.SUBMIT_EVIDENCE);
    }

    /**
     * Submits a proposal for a specified dispute by triggering the
     * state transition event associated with proposal submission.
     *
     * This method sends the {@code SUBMIT_PROPOSAL} event to the state machine of
     * the dispute identified by the provided dispute ID, indicating that a
     * proposal has been submitted for the dispute.
     *
     * @param disputeId the unique identifier of the dispute for which the proposal is being submitted
     */
    public void disputeProposalSubmitted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.SUBMIT_PROPOSAL);
    }

    /**
     * Accepts a proposal for a specified dispute by triggering the state transition event
     * associated with proposal acceptance.
     *
     * This method sends the {@code ACCEPT_PROPOSAL} event to the state machine of the
     * dispute identified by the provided dispute ID, indicating that the proposal has
     * been accepted for the dispute.
     *
     * @param disputeId the unique identifier of the dispute for which the proposal is accepted
     */
    public void disputeProposalAccepted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.ACCEPT_PROPOSAL);
    }

    /**
     * Accepts a resolution proposed for the middle stage of a dispute by triggering
     * the state transition event associated with middle acceptance.
     *
     * This method sends the {@code ACCEPT_MIDDLE} event to the state machine of the
     * dispute identified by the provided dispute ID, indicating that the middle
     * proposal has been accepted for the dispute.
     *
     * @param disputeId the unique identifier of the dispute for which the middle proposal is accepted
     */
    public void disputeMiddleAccepted(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.ACCEPT_MIDDLE);
    }

    /**
     * Rejects a proposal for a specified dispute by triggering the state transition event
     * associated with proposal rejection.
     *
     * This method sends the {@code REJECT_PROPOSAL} event to the state machine of the
     * dispute identified by the provided dispute ID, indicating that the proposal
     * has been rejected for the dispute.
     *
     * @param disputeId the unique identifier of the dispute for which the proposal is rejected
     */
    public void disputeProposalRejected(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.REJECT_PROPOSAL);
    }

    /**
     * Escalates a specified dispute by triggering the state transition event
     * associated with proposal rejection.
     *
     * This method sends the {@code REJECT_PROPOSAL} event to the state machine of the
     * dispute identified by the provided dispute ID, indicating that the dispute
     * has been escalated and a proposal has been rejected.
     *
     * @param disputeId the unique identifier of the dispute that is being escalated
     */
    public void disputeEscalated(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.REJECT_PROPOSAL);
    }

    /**
     * Marks a dispute as decided by an administrator by triggering the
     * corresponding state transition event.
     *
     * This method sends the {@code ADMIN_DECIDED} event to the state machine
     * associated with the dispute identified by the provided dispute ID,
     * indicating that the administrator has made a decision on the dispute.
     *
     * @param disputeId the unique identifier of the dispute for which the administrator decision is being applied
     */
    public void disputeAdminDecided(UUID disputeId) {
        fireEvent(disputeId, DisputeEvent.ADMIN_DECIDED);
    }
}